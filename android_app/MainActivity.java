package com.example.mameremotecontrol;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static final String CONNECT_IP = "com.example.mameremotecontrol.connect_ip";
    public static final String CONNECT_PORT = "com.example.mameremotecontrol.connect_port";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button buttonConnect = (Button) findViewById(R.id.button_connect);
        buttonConnect.setEnabled(false);

        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                checkInputValidity();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        EditText inputIp = (EditText) findViewById(R.id.input_ip);
        inputIp.addTextChangedListener(tw);

        EditText inputPort = (EditText) findViewById(R.id.input_port);
        inputPort.addTextChangedListener(tw);
    }

    private static boolean isValidIp(String ip) {
        try {
            if ( ip == null || ip.isEmpty() ) {
                return false;
            }

            String[] parts = ip.split( "\\." );
            if ( parts.length != 4 ) {
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {
                    return false;
                }
            }
            if ( ip.endsWith(".") ) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    private void checkInputValidity() {
        EditText inputIp = (EditText) findViewById(R.id.input_ip);
        EditText inputPort = (EditText) findViewById(R.id.input_port);
        Button buttonConnect = (Button) findViewById(R.id.button_connect);
        String portString = inputPort.getText().toString();
        int port = 3018;
        if (!portString.equals("")) {
            port = Integer.parseInt(portString);
        }
        buttonConnect.setEnabled(isValidIp(inputIp.getText().toString()) && port > 3000 && port < 3100);
    }

    public void onConnectClicked(View view) {
        Intent intent = new Intent(this, ConnectingActivity.class);
        EditText inputIp = (EditText) findViewById(R.id.input_ip);
        intent.putExtra(CONNECT_IP, inputIp.getText().toString());

        EditText inputPort = (EditText) findViewById(R.id.input_port);
        int port = 3018;
        String portString = inputPort.getText().toString();
        if (!portString.equals("")) {
            port = Integer.parseInt(portString);
        }
        intent.putExtra(CONNECT_PORT, port);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}