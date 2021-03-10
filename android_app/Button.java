package com.example.mameremotecontrol;

import android.graphics.Bitmap;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.HashMap;

public interface Button {
    Boolean isPressed();
    void setTouches(ArrayList<Touch> touches);
    HashMap<String, Integer> getKeyStatus();
    RectF getRect();
    Bitmap getBitmap();
}
