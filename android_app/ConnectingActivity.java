package com.example.mameremotecontrol;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;

public class ConnectingActivity extends Activity {
    private MameSocket _mameSocket;
    private ControllerView _glView;
    private ButtonMap _buttons;
    private String _previousJson = "";
    private Boolean _connected = false;
    private BitMapper _bm = new BitMapper();

    private class CallbackerImpl implements Callbacker {
        @Override
        public void isConnected(Boolean connected) {
            _connected = connected;
            _glView.setConnected(connected);
        }

        @Override
        public void setNumberOfButtons(int number) {
            Point size = getDimensions();
            _buttons = new ButtonMap(number, size.x, size.y, _bm);
            _glView.setButtons(_buttons);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        _glView = new ControllerView(this);
        _glView.setConnectingBitmap(_bm.getBitmap("connecting"));
        setContentView(_glView);

        _mameSocket = new MameSocket(getIpAddress(), getPort(), new CallbackerImpl());
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (!_connected) {
            return true;
        }
        int index = e.getActionIndex();
        ArrayList<Touch> touches = new ArrayList<>();
        for (int i = 0; i < e.getPointerCount(); i++) {
            if (!(index == i && e.getAction() == MotionEvent.ACTION_UP)) {
                touches.add(new Touch(e.getX(i), e.getY(i)));
            }
        }

        String json = _buttons.getTouches(touches);
        if (json.compareTo(_previousJson) != 0) {
            _mameSocket.sendMessage(json);
            _previousJson = json;
        }

        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        _mameSocket.stop();
    }

    private String getIpAddress() {
        Intent intent = getIntent();
        return intent.getStringExtra(MainActivity.CONNECT_IP);
    }

    private int getPort() {
        Intent intent = getIntent();
        return intent.getIntExtra(MainActivity.CONNECT_PORT, 3018);
    }

    private Point getDimensions() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        return new Point(width, height);
    }
}