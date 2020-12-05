-- license:BSD-3-Clause
-- copyright-holders:Ruben Van Dijck
local exports = {}
exports.name = 'remote_control'
exports.version = '0.0.1'
exports.description = 'Remote control plugin'
exports.license = 'The BSD 3-Clause License'
exports.author = { name = 'Ruben Van Dijck' }

local remote_control = exports

function sleep (a)
    local sec = tonumber(os.clock() + a);
    while (os.clock() < sec) do
    end
end

function get_ip_address()
    n = os.tmpname()
    os.execute("hostname -I | awk '{print $1}' > " .. n)
    file = io.open(n, "r")
    io.input(file)
    ip_address = io.read()
    io.close(file)
    os.remove(n)
    return ip_address
end

function set_contains(set, label)
    return set[label] ~= nil
end

Player = { buttons = nil, connected = false, socket = nil, port = 0, button_codes = nil}

function Player:new (port)
    o = {}
    setmetatable(o, self)
    self.__index = self
    self.port = port
    return o
end

function Player:open_socket()
    if self.socket ~= nil then
        self.socket:close()
    end
    self.socket = emu.file("rwc")
    local err = self.socket:open('socket.127.0.0.1:' .. self.port)
    if err then
        print("Couldn't open port " .. port_number)
        error("Couldn't open port " .. port_number)
    end
end

function create_button_code(n, button_amount)
    buttons = { down = "P" .. n .. " Down",
                up = "P" .. n .. " Up",
                left = "P" .. n .. " Left",
                right = "P" .. n .. " Right",
                start = "" .. n .. " Player Start"
              }
    for i=1,button_amount do
        buttons["button" .. i] = "P" .. n .." Button " .. i
    end

    return buttons
end

function remote_control.startplugin()
    local json = require('json')

    local port_number = 3018
    local game_running = false
    local waiting_for_players = true
    local amount_of_buttons = 0

    local players = {}

    local ip_address = get_ip_address()

    local function draw_qr()
        s = manager:machine().screens[":screen"]
        s:draw_box(0, 0, 100, 20, 0xff000000, 0xff000000);
    end

    local function list_players()
        s = manager:machine().screens[":screen"]
        s:draw_text(4, 4, ip_address)
    end

    local function check_sock_data(player)
        local data = player.socket:read(100)
        if #data == 0 then
            return
        end

        msg_json = json.parse(data)
        if not msg_json then
            print("Couldn't parse incoming json.")
            return
        end

        -- TODO check for ping

        for k, v in pairs(player.button_codes) do
            player.buttons[v]:set_value(msg_json[k])
        end

        io.write(data)
    end

  local frames = 0
    local frames_before_refresh = 200

    local function check_for_refresh()
        frames = frames + 1
        if frames == frames_before_refresh then
            ip_address = get_ip_address()
            frames = 0
        end
    end

    local function frame_callback()
        if game_running then
            if waiting_for_players then
                check_for_refresh()
                draw_qr()
                list_players()
            end

            for k,v in pairs(players) do
                check_sock_data(v)
            end
        end
    end

    local function create_player(index)
        player = nil
        local ports = manager:machine():ioport().ports
        if set_contains(ports, ":P" .. index) then
            player = Player:new(port_number + index - 1)
            player.buttons = ports[":P" .. index].fields

            for k,v in pairs(player.buttons) do
                if string.match(k, "^P. Button .$") then
                    amount_of_buttons = amount_of_buttons + 1
                end
            end

            if amount_of_buttons > 6 then
                amount_of_buttons = 6
            end

            player.button_codes = create_button_code(index, amount_of_buttons)
            player:open_socket()
        end
        return player
    end

    local function start_callback()
        game_running = emu.gamename() ~= 'No Driver Loaded'
        if game_running then
            index = 1
            local player = create_player(index)
            while player ~= nil do
                players[index] = player
                index = index + 1
                player = create_player(index)
            end
            if p1 == nil then
                -- TODO show error message
            end
        end
    end

    emu.register_start(start_callback)
    emu.register_frame_done(frame_callback)
end

return exports
