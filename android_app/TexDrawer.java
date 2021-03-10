package com.example.mameremotecontrol;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_SRC_ALPHA;

public class TexDrawer {
    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
            "attribute vec2 a_TexCoordinate;" +
            "varying vec2 v_TexCoordinate; " +
            "void main() {" +
            "  gl_Position = vPosition;" +
            "  v_TexCoordinate = a_TexCoordinate;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform sampler2D u_Texture;" +
            "varying vec2 v_TexCoordinate;" +
            "void main() {" +
            "  gl_FragColor = vec4(texture2D(u_Texture, v_TexCoordinate));" +
            "}";

    static final int COORDS_PER_VERTEX = 3;
    private FloatBuffer vertexBuffer;
    private FloatBuffer _texCoordsBuffer;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private int _glProgram;
    private float _width;
    private float _height;

    public TexDrawer() {
        _width = 0;
        _height = 0;
        _glProgram = createProgram();
        _texCoordsBuffer = createTexCoords();
    }

    public void setViewSize(int w, int h) {
        _width = w;
        _height = h;
    }

    private FloatBuffer createTexCoords() {
        final float[] cubeTextureCoordinateData = {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };

        FloatBuffer texCoords = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        texCoords.put(cubeTextureCoordinateData).position(0);
        return texCoords;
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    private int createProgram() {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        return program;
    }

    private float transformX(float x) {
        return (x / _width) * 2.0f - 1.0f;
    }

    private float transformY(float y) {
        return - ((y / _height) * 2.0f - 1.0f);
    }

    private void loadRect(float x1, float y1, float x2, float y2) {
        float rectCoords[] = {
                transformX(x1), transformY(y1), 0.0f,
                transformX(x1), transformY(y2), 0.0f,
                transformX(x2), transformY(y2), 0.0f,
                transformX(x1), transformY(y1), 0.0f,
                transformX(x2), transformY(y2), 0.0f,
                transformX(x2), transformY(y1), 0.0f
        };

        ByteBuffer bb = ByteBuffer.allocateDirect(rectCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(rectCoords);
        vertexBuffer.position(0);
    }

    public void drawRect(RectF rect, Bitmap bm) {
        loadRect(rect.left, rect.top, rect.right, rect.bottom);
        int textureHandle = loadTexture(bm);

        GLES20.glUseProgram(_glProgram);

        int positionHandle = GLES20.glGetAttribLocation(_glProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        GLES20.glBlendFunc(GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(_glProgram, "u_Texture"), 0);
        GLES20.glVertexAttribPointer(GLES20.glGetAttribLocation(_glProgram, "a_TexCoordinate"),
                2, GLES20.GL_FLOAT, false,
                0, _texCoordsBuffer);

        GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(_glProgram, "a_TexCoordinate"));

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    public static int loadTexture(Bitmap bm)
    {
        final int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);
        if (textureHandle[0] != 0)
        {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bm, 0);
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }
}
