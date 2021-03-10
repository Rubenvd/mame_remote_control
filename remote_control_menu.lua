local lib = {}
local port = 3018
local config_file = "remote_control.cfg"

local function get_settings_path()
	return lfs.env_replace(manager:machine():options().entries.homepath:value():match('([^;]+)')) .. '/remote_control/'
end

function lib:get_port()
    return port
end

function lib:load_settings()
    local file = io.open(get_settings_path() .. config_file, 'r')
    if not file then
        return
    end
    port = tonumber(file:read('a'))
    file:close()
end

function lib:save_settings()
    local path = get_settings_path()
    local attr = lfs.attributes(path)
    if not attr then
        lfs.mkdir(path)
    elseif attr.mode ~= 'directory' then
        return
    end
    local file = io.open(path .. config_file, 'w')
    if file then
        file:write(port)
        file:close()
    end
end

function lib:populate_menu()
    local menu = {}
	menu[#menu + 1] = {_('Remote control port'), port, port > 3000 and port < 3100 and 'lr' or 'r' or 'l'}
	return menu
end

function lib:handle_menu_event(event)
	manager:machine():popmessage()
    print("event is " .. event)
    if event == 'left' and port > 3001 then
        print("left")
        port = port - 1
    elseif event == 'right' and port < 3099 then
        print("right")
        port = port + 1
    end
    return true
end

return lib
