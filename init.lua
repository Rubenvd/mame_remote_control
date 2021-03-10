-- license:BSD-3-Clause
-- copyright-holders:Ruben Van Dijck
local exports = {}
exports.name = 'remote_control'
exports.version = '0.0.1'
exports.description = 'Remote control plugin'
exports.license = 'The BSD 3-Clause License'
exports.author = { name = 'Ruben Van Dijck' }

local remote_control = exports

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

Player = { buttons = nil, connected = false, socket = nil, frames_since_message = 0}

function Player:new ()
    o = {}
    setmetatable(o, self)
    self.__index = self
    o.buttons = {}
    o.socket = emu.file("rwc")
    return o
end

function Player:open_socket(socket_port)
    local err = self.socket:open('socket.0.0.0.0:' .. socket_port)
    if err then
        return false
    end
    return true
end

function Player:destroy()
    self.connected = false
    self.frames_since_message = 0
    -- Open a file that can't exist so we close the socket
    self.socket:open('socket.0.0.0.300:-1')
end

function remote_control.startplugin()
    local json = require('json')

    local server_port = 3018
    local game_running = false
    local show_info = true
    local amount_of_buttons = 0
    local server_socket = emu.file('rwc')

    local players = {}

    local ip_address = get_ip_address()

    local function draw_qr()
        s = manager:machine().screens[":screen"]
        s:draw_box(0, 0, 100, 20, 0xff000000, 0xff000000);
    end

    local function list_players()
        s = manager:machine().screens[":screen"]
        s:draw_text(4, 4, "IP address: " .. ip_address .. "\nPort: " .. server_port)
    end

    local function check_player_socket(player)
        player.frames_since_message = player.frames_since_message + 1
        local data = player.socket:read(100)
        if #data == 0 then
            if player.frames_since_message == 400 then
                emu.print_info("Player didn't ping for a while, closing connection ")
                player:destroy()
            end
            return
        end

        player.frames_since_message = 0

        if data == "ping" then
            player.socket:write("ping\n");
        else
            msg_json = json.parse(data)
            if not msg_json then
                print("Couldn't parse incoming json.")
                return
            end

            player.socket:write("ping\n")

            for k, v in pairs(player.buttons) do
                if set_contains(msg_json, k) then
                    v[2] = msg_json[k]
                end
            end

            if set_contains(msg_json, "i") then
                if msg_json["i"] == 1 then
                    show_info = not show_info
                end
            end
        end
    end

    local frames = 0
    local frames_before_refresh = 200

    local function check_for_refresh()
        if frames == frames_before_refresh then
            ip_address = get_ip_address()
            frames = 0
        end
    end

    local function update_player_keys(player)
        if player.frames_since_message > 10 then
            return
        end

        for k, v in pairs(player.buttons) do
            if v[1] ~= nil then
                v[1]:set_value(v[2])
            end
        end
    end

    local function check_server_socket()
        local data = server_socket:read(100)
        if #data == 0 then
            return
        end
        for i=1,#players do
            if not players[i].connected then
                players[i].connected = true
                attempted_port = server_port + 1
                while not players[i]:open_socket(attempted_port) and attempted_port < (server_port + 100) do
                    attempted_port = attempted_port + 1
                end
                if attempted_port == (server_port + 100) then
                    print("Couldn't open port for new player")
                    return
                end
                server_socket:write("{\"port\": " .. attempted_port .. ",\"buttons\": " .. amount_of_buttons .. "}\n")
                server_socket:open('socket.0.0.0.0:' .. server_port)
                return
            end
        end
    end

    local function frame_callback()
        if game_running then
            frames = frames + 1
            if show_info then
                check_for_refresh()
                draw_qr()
                list_players()
            end

            check_server_socket()

            for k,v in pairs(players) do
                if v.connected then
                    check_player_socket(v)
                    update_player_keys(v)
                end
            end
        end
    end

    local function getIoPort(keyString)
        local ports = manager:machine():ioport().ports
        for portKey,portVal in pairs(ports) do
            for fieldKey, fieldVal in pairs(ports[portKey].fields) do
                if string.match(fieldKey, keyString) then
                    return fieldVal
                end
            end
        end
        return nil
    end

    local function foundPorts(player)
        for k,v in pairs(player.buttons) do
            if v[1] ~= nil then
                return true
            end
        end
        return false
    end

    local function create_player(index)
        player = Player:new()

        player.buttons["u"] = {getIoPort("P" .. index .. " Up"), 0}
        player.buttons["c"] = {getIoPort("Coin " .. index), 0}
        player.buttons["d"] = {getIoPort("P" .. index .. " Down"), 0}
        player.buttons["l"] = {getIoPort("P" .. index .. " Left"), 0}
        player.buttons["r"] = {getIoPort("P" .. index .. " Right"), 0}
        player.buttons["s"] = {getIoPort("" .. index .. ".*Start"), 0}

        for i=1,amount_of_buttons do
            player.buttons["b" .. i] = {getIoPort("P" .. index .. " Button " .. i), 0}
        end

        if foundPorts(player) then
            return player
        end

        return nil
    end

    local function get_amount_of_buttons()
        local ports = manager:machine():ioport().ports
        local button_count = 0
        for portKey,portVal in pairs(ports) do
            for fieldKey, fieldVal in pairs(ports[portKey].fields) do
                if string.match(fieldKey, "^P1 Button .$") then
                    button_count = button_count + 1
                end
            end
            if button_count ~= 0 then
                if button_count > 6 then
                    button_count = 6
                end
                return button_count
            end
        end
        return button_count
    end

    local function cleanup()
        for k,v in pairs(players) do
            v:destroy()
        end
        players = {}
    end

    local function start_server_socket()
        server_socket:open('socket.0.0.0.0:' .. server_port)
    end

    local function get_port()
        local menu_handler = require('remote_control/remote_control_menu')
    if menu_handler then
            menu_handler:load_settings()
                return menu_handler:get_port()
        else
            return 3018
        end
    end

    local function start_callback()
        cleanup()
        server_port = get_port()

        game_running = emu.gamename() ~= 'No Driver Loaded'
        if game_running then
            index = 1
            amount_of_buttons = get_amount_of_buttons()
            local player = create_player(index)
            local coinPort = nil
            if player ~= nil then
                coinPort = player.buttons["c"]
            end
            while player ~= nil do
                players[index] = player
                index = index + 1
                player = create_player(index)
                if player ~= nil and player.buttons["c"] == nil then
                    player.buttons["c"] = coinPort
                end
            end

            start_server_socket()
        end
    end

    local function stop_callback()
        local menu_handler = require('remote_control/remote_control_menu')
        if menu_handler then
            menu_handler:save_settings()
        end
    end

    local function menu_callback(index, event)
        local menu_handler = require('remote_control/remote_control_menu')
        if menu_handler then
            local ret = menu_handler:handle_menu_event(event)
            server_port = menu_handler:get_port()
            start_server_socket()
            return ret
        else
            return false
        end
    end

    local function menu_populate()
        local menu_handler = require('remote_control/remote_control_menu')
        if menu_handler then
            return menu_handler:populate_menu()
        else
            return {{_('Failed to load remote control menu'), '', ''}}
        end
    end

    emu.register_menu(menu_callback, menu_populate, _('Remote control'))
    emu.register_start(start_callback)
    emu.register_stop(stop_callback)
    emu.register_frame_done(frame_callback)
end

return exports

