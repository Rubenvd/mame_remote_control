package com.example.mameremotecontrol;

import android.graphics.Bitmap;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.HashMap;

public class RectButton implements Button {
    private float _x1;
    private float _y1;
    private float _x2;
    private float _y2;
    private String _keyString;
    private Bitmap _bm;
    private Boolean _pressed = false;

    public RectButton(float x1, float y1, float x2, float y2, String keyString, Bitmap bm) {
        _x1 = x1;
        _y1 = y1;
        _x2 = x2;
        _y2 = y2;
        _keyString = keyString;
        _bm = bm;
    }

    @Override
    public Boolean isPressed() {
        return _pressed;
    }

    @Override
    public void setTouches(ArrayList<Touch> touches) {
        for (Touch t : touches) {
            float x = t.getX();
            float y = t.getY();
            if (x > _x1 && x < _x2 && y > _y1 && y < _y2) {
                _pressed = true;
                return;
            }
        }
        _pressed = false;
    }

    @Override
    public HashMap<String, Integer> getKeyStatus() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put(_keyString, _pressed ? 1 : 0);
        return map;
    }

    @Override
    public RectF getRect() {
        return new RectF(_x1, _y1, _x2, _y2);
    }

    @Override
    public Bitmap getBitmap() {
        return _bm;
    }
}
