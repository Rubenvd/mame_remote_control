package com.example.mameremotecontrol;

import android.graphics.Bitmap;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.HashMap;

public class ArrowButton implements Button{
    private Boolean _upPressed = false;
    private Boolean _downPressed = false;
    private Boolean _leftPressed = false;
    private Boolean _rightPressed = false;

    private float _x;
    private float _y;
    private float _radius;
    private float _minTouchRadius;
    private Bitmap _bm;

    public ArrowButton(float x, float y, float radius, Bitmap bm) {
        _x = x;
        _y = y;
        _radius = radius;
        _bm = bm;
        _minTouchRadius = _radius / 4;
    }

    @Override
    public Boolean isPressed() {
        return false;
    }

    @Override
    public void setTouches(ArrayList<Touch> touches) {
        _upPressed = false;
        _downPressed = false;
        _leftPressed = false;
        _rightPressed = false;
        final float touchEdge = (float)Math.sin(Math.toRadians(360 / 16));

        for (Touch t : touches) {
            float dx = t.getX() - _x;
            float dy = t.getY() - _y;
            float dist = (float) Math.sqrt((double)((dx*dx) + (dy*dy)));
            if (dist < _radius && dist > _minTouchRadius) {
                float sin = dy / dist;
                float cos = dx / dist;
                if (sin > touchEdge) {
                    _downPressed = true;
                } else if (sin < -touchEdge) {
                    _upPressed = true;
                }

                if (cos > touchEdge) {
                    _rightPressed = true;
                } else if (cos < -touchEdge) {
                    _leftPressed = true;
                }
            }
        }
    }

    @Override
    public HashMap<String, Integer> getKeyStatus() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("u", _upPressed ? 1 : 0);
        map.put("d", _downPressed ? 1 : 0);
        map.put("l", _leftPressed ? 1 : 0);
        map.put("r", _rightPressed ? 1 : 0);
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
