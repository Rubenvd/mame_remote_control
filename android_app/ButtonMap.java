package com.example.mameremotecontrol;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ButtonMap {
    private ArrayList<Button> _buttons = new ArrayList<>();
    private BitMapper _bitMapper;

    public ButtonMap(int buttons, int x, int y, BitMapper bm) {
        _bitMapper = bm;
        float middle_portion = 0.15f;
        float middle_left_border = x * (0.5f - (middle_portion / 2.0f));
        float middle_right_border = x * (0.5f + (middle_portion / 2.0f));
        float button_radius = 0;
        switch(buttons) {
            case 1:
                button_radius = (x - middle_right_border) * 0.4f;
                _buttons.add(new RoundButton((x * 0.95f)  - button_radius , (y * 0.95f) - button_radius, button_radius, "b1", _bitMapper.getBitmap("b1")));
                break;
            case 2:
                button_radius = (x - middle_right_border) * 0.2f;
                _buttons.add(new RoundButton(x - button_radius, (y * 0.70f) - button_radius, button_radius, "b1", _bitMapper.getBitmap("b1")));
                _buttons.add(new RoundButton(x - 3.0f * button_radius, (y * 0.95f) - button_radius, button_radius, "b2", _bitMapper.getBitmap("b2")));
                break;
            case 3:
                button_radius = (x - middle_right_border) * 0.2f;
                _buttons.add(new RoundButton(x - button_radius, (y * 0.95f) - button_radius, button_radius, "b1", _bitMapper.getBitmap("b1")));
                _buttons.add(new RoundButton(x - 3.0f * button_radius, (y * 0.95f) - (1.1f *button_radius), button_radius, "b2", _bitMapper.getBitmap("b2")));
                _buttons.add(new RoundButton(x - button_radius, (y * 0.95f) - (3.1f* button_radius), button_radius, "b3", _bitMapper.getBitmap("b3")));
                break;
            case 4:
                button_radius = (x - middle_right_border) * 0.2f;
                _buttons.add(new RoundButton(x - button_radius, (y * 0.95f) - button_radius, button_radius, "b1", _bitMapper.getBitmap("b1")));
                _buttons.add(new RoundButton(x - 3.0f * button_radius, (y * 0.95f) - (1.1f *button_radius), button_radius, "b2", _bitMapper.getBitmap("b2")));
                _buttons.add(new RoundButton(x - button_radius, (y * 0.95f) - (3.1f* button_radius), button_radius, "b3", _bitMapper.getBitmap("b3")));
                _buttons.add(new RoundButton(x - 3.0f * button_radius, (y * 0.95f) - (3.1f* button_radius), button_radius, "b4", _bitMapper.getBitmap("b4")));
                break;
        }
        float arrow_radius = middle_left_border * 0.4f;
        _buttons.add(new ArrowButton((middle_left_border * 0.05f) + arrow_radius, (y * 0.95f) - arrow_radius, arrow_radius, _bitMapper.getBitmap("arrows")));

        _buttons.add(new RoundButton(x * 0.05f + y*0.05f, x * 0.05f + y * 0.05f,  x * 0.05f, "c", _bitMapper.getBitmap("c")));
        _buttons.add(new RoundButton(x * 0.16f + y*0.05f, x * 0.05f + y * 0.05f,  x * 0.05f, "i", _bitMapper.getBitmap("i")));

        float startY = y * 0.05f;
        _buttons.add(new RectButton(middle_left_border, startY , middle_right_border, startY + (x * 0.15f * 9.0f / 31.0f), "s", _bitMapper.getBitmap("s")));
    }

    public String getTouches(ArrayList<Touch> touches) {
        try {
            JSONObject json = new JSONObject();
            for (Button b: _buttons) {
                b.setTouches(touches);
                HashMap<String, Integer> status = b.getKeyStatus();
                Iterator it = status.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    json.put((String)pair.getKey(), pair.getValue());
                    it.remove();
                }
            }

            return json.toString();
        } catch(JSONException e){
            System.out.printf("JSON exception");
            e.printStackTrace();
        }

        return "";
    }

    public ArrayList<Button> getButtons() {
        return _buttons;
    }
}
