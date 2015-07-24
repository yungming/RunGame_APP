package com.example.cynthia.runrunrun;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

    private Button btsubmit;
    private String IMEI;
    private TextView tvIMEI;
    private EditText etName, etCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btsubmit = (Button)findViewById(R.id.btsubmit);
        btsubmit.setOnClickListener(btsubmitListener);

        tvIMEI = (TextView)findViewById(R.id.tvIMEI);
        etName = (EditText)findViewById(R.id.etName);
        etCode = (EditText)findViewById(R.id.etCode);

        //get IMEI code
        TelephonyManager mTelManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = mTelManager.getDeviceId();

        tvIMEI.setText(""+IMEI);
    }

    private Button.OnClickListener btsubmitListener = new Button.OnClickListener(){

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, PedometerActivity.class);

            //send data
            Bundle bundle = new Bundle();
            String UserName = etName.getText().toString();
            String UserCode = etCode.getText().toString();
            bundle.putString("etName", UserName);
            bundle.putString("etCode", UserCode);

            intent.putExtras(bundle);

            startActivity(intent);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
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
