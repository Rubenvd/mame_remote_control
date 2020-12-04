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

Player = { buttons = nil,
					 connected = false,
				   socket = nil,
				   port = 0
				   }

function Player:new (port)
  o = o or {}
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

function remote_control.startplugin()
	local port_number = 3018
	local game_running = false
	local waiting_for_players = true

	local p1 = Player:new(port_number)
	p1:open_socket()

	if err then
		print("Couldn't open port " .. port_number)
		error("Couldn't open port " .. port_number)
	end

	local ip_address = get_ip_address()

	local function draw_qr()
		s = manager:machine().screens[":screen"]
		s:draw_box(0, 0, 100, 20, 0xff000000, 0xff000000);
	end

	local function list_players()
		s = manager:machine().screens[":screen"]
		s:draw_text(4, 4, ip_address)
	end

	local function check_sock_data()
		local data = p1.socket:read(100)

		if data:match "^.*hello.*$" then
			waiting_for_players = false
		end

		if data:match "^.*hit.*$" then
			p1.buttons["P1 Button 1"]:set_value(1)
		end

		if data:match "^.*stop.*$" then
			p1.buttons["P1 Button 1"]:set_value(0)
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

	local function process_input()
		if p1.buttons ~= nil then

		end
	end

	local function frame_callback()
		if game_running then
			if waiting_for_players then
				check_for_refresh()
				draw_qr()
				list_players()
			end

			check_sock_data()
			process_input()
		end
	end

	local function start_callback()
		print("START")
		game_running = emu.gamename() ~= 'No Driver Loaded'
		local ports = manager:machine():ioport().ports

		if set_contains(ports, ":P1") then
			p1.buttons = ports[":P1"].fields
		end
	end

	local function menu_callback(index, event)
		local menu_handler = require('remote_control/remote_control_menu')
		if menu_handler then
			return menu_handler:handle_menu_event(index, event, buttons)
		else
			return false
		end
	end

	local function menu_populate()
		local menu_handler = require('remote_control/remote_control_menu')
		if menu_handler then
			return menu_handler:populate_menu(buttons)
		else
			return {{_('Failed to load remote control menu'), '', ''}}
		end
	end

	-- emu.register_menu(menu_callback, menu_populate, _('Remote control'))
	emu.register_start(start_callback)
	emu.register_frame_done(frame_callback)
end

return exports
