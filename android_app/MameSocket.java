package com.example.mameremotecontrol;

public class MameSocket {
    private MameSocketThread _sock;

    public MameSocket(String ipAddress, int port, Callbacker cb) {
        _sock = new MameSocketThread(ipAddress, port, cb);
        new Thread(_sock).start();
    }

    public void sendMessage(String msg) {
        _sock.sendMessage(msg);
    }

    public void stop() { _sock.stop(); }
}
