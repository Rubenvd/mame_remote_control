package com.example.mameremotecontrol;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MameSocketThread implements Runnable {
    private String _ipAddress;
    private int _port;
    private Object _mutex = new Object();
    private Boolean _parsed = false;
    private String _toSend = "";
    private Callbacker _callbacker;
    private AtomicBoolean _stopThread = new AtomicBoolean();

    public MameSocketThread(String ipAddress, int port, Callbacker callbacker) {
        _ipAddress = ipAddress;
        _port = port;
        _callbacker = callbacker;
        _stopThread.set(false);
    }

    private class SocketObj {
        private Socket _sock;
        private PrintWriter _out;
        private BufferedReader _in;
        private String _sockIp;
        private int _sockPort;

        public SocketObj(String ipAddress, int port) {
            _sockIp = ipAddress;
            _sockPort = port;
        }

        public Boolean connect() {
            try {
                // Connect socket
                InetAddress serverAddr = InetAddress.getByName(_sockIp);
                _sock = new Socket(serverAddr, _sockPort);

                // Create pipes
                _out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(_sock.getOutputStream())),
                        true);
                _in = new BufferedReader(new InputStreamReader(_sock.getInputStream()));
                return true;
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            close();
            return false;
        }

        public void write(String s) {
            _out.write(s);
            _out.flush();
        }

        public String read() {
            try {
                if (!_in.ready()) {
                    return "";
                }
                return _in.readLine();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return "";
        }

        public String readBlocking() {
            try {
                return _in.readLine();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return "";
        }

        public void close() {
            try {
                if (_sock != null) {
                    _sock.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void processMessages(SocketObj socket) {
        int pingCounter = 0;
        while (true) {
            pingCounter += 1;
            // Process output
            synchronized (_mutex) {
                if (!_parsed) {
                    socket.write(_toSend);
                    _parsed = true;
                }
            }

            if (pingCounter == 400) {
                socket.write("ping");
            }
            // Process input
            String line = socket.read();
            if (line != "") {
                pingCounter = 0;
            }
            if (_stopThread.get() || pingCounter > 800) {
                _callbacker.isConnected(false);
                return;
            }
            rest(10);
        }
    }

    class ClientPortConfig {
        private int _port;
        private int _numberOfButtons;

        public ClientPortConfig(int port, int numberOfButtons) {
            _port = port;
            _numberOfButtons = numberOfButtons;
        }

        public int getPort() {
            return _port;
        }

        public int getNumberOfButtons() {
            return _numberOfButtons;
        }
    }

    private ClientPortConfig getConfigFromString(String str) {
        try {
            JSONObject obj = new JSONObject(str);
            return new ClientPortConfig(obj.getInt("port"), obj.getInt("buttons"));
        } catch (JSONException e) {
            System.out.println("Couldn't parse json: " + str);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {
        SocketObj socket = new SocketObj(_ipAddress, _port);
        while (!_stopThread.get()) {
            if (socket.connect()) {
                socket.write("olla");
                ClientPortConfig config = getConfigFromString(socket.readBlocking());
                if (config != null) {
                    socket.close();
                    SocketObj clientSocket = new SocketObj(_ipAddress, config.getPort());
                    if (clientSocket.connect()) {
                        _callbacker.isConnected(true);
                        _callbacker.setNumberOfButtons(config.getNumberOfButtons());
                        processMessages(clientSocket);
                    }
                    clientSocket.close();
                }
            }
            socket.close();
            rest(2000);
        }
    }

    public void sendMessage(String message) {
        synchronized (_mutex) {
            _parsed = false;
            _toSend = message;
        }
    }

    public void stop() {
        _stopThread.set(true);
    }

    private void rest(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e1) {
            System.out.println("Don't know...");
        }
    }
}
