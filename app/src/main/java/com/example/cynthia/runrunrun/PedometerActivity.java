package com.example.cynthia.runrunrun;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;


public class PedometerActivity extends ActionBarActivity implements SensorEventListener {


    private TextView StepText, UserName, UserCode;
    private String msgs,IMEI,strName;
    private ToggleButton tgbOnOff;//�������s

    private SensorManager aSensorManager;
    private int gravityRate=5000;

    public static int CURRENT_SETP = 0;
    public static float SENSITIVITY = 10; // Sensitivity
    private float mLastValues[] = new float[3 * 2];
    private float mScale[] = new float[2];
    private float mYOffset;
    private static long end = 0;
    private static long start = 0;

    //Last acceleration direction
    private float mLastDirections[] = new float[3 * 2];
    private float mLastExtremes[][] = { new float[3 * 2], new float[3 * 2] };
    private float mLastDiff[] = new float[3 * 2];
    private int mLastMatch = -1;
    int h = 480;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedometer);

        StepText = (TextView)this.findViewById(R.id.StepText);
        UserName = (TextView)this.findViewById(R.id.UserName);
        UserCode = (TextView)this.findViewById(R.id.UserCode);
        tgbOnOff = (ToggleButton) findViewById(R.id.TgbOnOff);

        aSensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        aSensorManager.registerListener(this,
                aSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), gravityRate * 100);

        //receive data
        Bundle bundle = this.getIntent().getExtras();
        UserName.setText("Hello!"+bundle.getString("etName")+".");
        UserCode.setText("your code:"+bundle.getString("etCode"));

        IMEI = bundle.getString("IMEI");
        strName = bundle.getString("etName");

        //�������s����{��
        tgbOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {//button ON
                    onResume();

                } else {//button Off
                    CURRENT_SETP = 0;
                    StepText.setText("0");
                    onPause();
                }
            }
        });

        mYOffset = h * 0.5f;
        mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        mScale[1] = -(h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
    }
    //Pedometer Start---
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    //When the sensor detects a change in the value of this method

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        synchronized (this) {
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                float vSum = 0;
                for (int i = 0; i < 3; i++) {
                    final float v = mYOffset + event.values[i] * mScale[1];
                    vSum += v;
                }
                int k = 0;
                float v = vSum / 3;

                float direction = (v > mLastValues[k] ? 1
                        : (v < mLastValues[k] ? -1 : 0));
                if (direction == -mLastDirections[k]) {
                    // Direction changed
                    int extType = (direction > 0 ? 0 : 1); // minumum or maximum?
                    mLastExtremes[extType][k] = mLastValues[k];
                    float diff = Math.abs(mLastExtremes[extType][k]
                            - mLastExtremes[1 - extType][k]);

                    if (diff > SENSITIVITY) {
                        boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k] * 2 / 3);
                        boolean isPreviousLargeEnough = mLastDiff[k] > (diff / 3);
                        boolean isNotContra = (mLastMatch != 1 - extType);

                        if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                            end = System.currentTimeMillis();
                            if (end - start > 10) {

                            // At this time it is determined that taking a step

                                CURRENT_SETP++;
                                mLastMatch = extType;
                                start = end;
                                StepText.setText(""+CURRENT_SETP);

                                msgs=""+CURRENT_SETP;
                                Thread x = new Thread(new sendPostRunnable(msgs));
                                x.start();
                            }
                        } else {
                            mLastMatch = -1;
                        }
                    }
                    mLastDiff[k] = diff;
                }
                mLastDirections[k] = direction;
                mLastValues[k] = v;
            }

        }
    }

    @Override
    protected void onPause()
    {
        // TODO Auto-generated method stub
    //�������USensorEventListener
        aSensorManager.unregisterListener(this);
        super.onPause();
    }

    protected void onResume() {

        aSensorManager.registerListener(this,
                aSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), gravityRate * 1000);
        super.onResume();
    }
    //---Pedometer End


    private String uriAPI = "http://http://shankmc.no-ip.org:8080/RunGame/httpposttest.php";
    /** �u�n��s�����v���T���N�X */
    protected static final int REFRESH_DATA = 0x00000001;

    /** �إ�UI Thread�ϥΪ�Handler�A�ӱ�����LThread�Ӫ��T�� */
    Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                // ��ܺ����W��������
                case REFRESH_DATA:
                    String result = null;
                    if (msg.obj instanceof String)
                        result = (String) msg.obj;
                    if (result != null)
                        // �L�X�����^�Ǫ���r
                        //show.setText(result);
                        break;
            }
        }
    };

    class sendPostRunnable implements Runnable
    {
        String strStep = null;
        // �غc�l�A�]�w�n�Ǫ��r��
        public sendPostRunnable(String strStep)
        {
            this.strStep = strStep;
        }
        @Override
        public void run()
        {
            String result = sendPostDataToInternet(strStep);
            mHandler.obtainMessage(REFRESH_DATA, result).sendToTarget();
        }
    }

    private String sendPostDataToInternet(String strStep)
    {

		//Create HTTP Post connection
        HttpPost httpRequest = new HttpPost(uriAPI);
		//Post�B�@�ǰe�ܼƥ�����NameValuePair[]�}�C�x�s

        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("IMEI", IMEI));
        params.add(new BasicNameValuePair("Step", strStep));
        params.add(new BasicNameValuePair("Name", strName));


        try

        {
			/* �o�XHTTP request */
            httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			/* ���oHTTP response */
            HttpResponse httpResponse = new DefaultHttpClient()
                    .execute(httpRequest);
			/* �Y���A�X��200 ok */
            if (httpResponse.getStatusLine().getStatusCode() == 200)
            {
				/* ���X�^���r�� */
                String strResult = EntityUtils.toString(httpResponse
                        .getEntity());
                // �^�Ǧ^���r��
                return strResult;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_pedometer, menu);
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
