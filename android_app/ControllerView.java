package com.example.mameremotecontrol;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;

public class ControllerView extends GLSurfaceView {
    private ControllerRenderer _renderer;

    public ControllerView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        _renderer = new ControllerRenderer();
        setRenderer(_renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void setButtons(ButtonMap buttons) {
        _renderer.setButtons(buttons);
        requestRender();
    }

    public void setConnected(Boolean connected) {
        _renderer.setConnected(connected);
        requestRender();
    }

    public void setConnectingBitmap(Bitmap bm) {
        _renderer.setConnectingBitmap(bm);
    }
}
