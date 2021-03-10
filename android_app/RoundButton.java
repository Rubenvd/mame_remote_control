package com.example.mameremotecontrol;

import android.graphics.Bitmap;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.HashMap;

public class RoundButton implements Button {
    private float _x;
    private float _y;
    private float _radius;
    private String _keyString;
    private Bitmap _bm;
    private Boolean _pressed = false;

    public RoundButton(float x, float y, float radius, String keyString, Bitmap bm) {
        _x = x;
        _y = y;
        _radius = radius;
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
            float dx = t.getX() - _x;
            float dy = t.getY() - _y;
            if ((dx*dx) + (dy*dy) < _radius * _radius) {
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
        return new RectF(_x - _radius, _y - _radius, _x + _radius, _y + _radius );
    }

    @Override
    public Bitmap getBitmap() {
        return _bm;
    }
}
