package com.example.mameremotecontrol;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ControllerRenderer implements GLSurfaceView.Renderer {
    ButtonMap _buttons;
    TexDrawer _texDrawer;
    Boolean _connected = false;
    Bitmap _connectingBitmap = null;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        _texDrawer = new TexDrawer();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        _texDrawer.setViewSize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        if (_connected && _buttons != null) {
            for (Button b : _buttons.getButtons()) {
                _texDrawer.drawRect(b.getRect(), b.getBitmap());
            }
        } else if (_connectingBitmap != null){
            _texDrawer.drawRect(new RectF(10f, 10f, 10f + (3f * 57f), 10f + (3f * 8f)), _connectingBitmap);
        }
    }

    public void setConnectingBitmap(Bitmap bm) {
        _connectingBitmap = bm;
    }

    public void setButtons(ButtonMap buttons){
        _buttons = buttons;
    }

    public void setConnected(Boolean connected) { _connected = connected; }
}
