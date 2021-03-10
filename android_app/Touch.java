package com.example.mameremotecontrol;

public class Touch {
    private float _x;
    private float _y;

    public Touch(float x, float y) {
        _x = x;
        _y = y;
    }

    public float getX() {
        return _x;
    }

    public float getY() {
        return _y;
    }
}
