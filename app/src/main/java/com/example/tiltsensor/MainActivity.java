package com.example.tiltsensor;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;

    private Sensor mSensorAccelerometer;
    private Sensor mSensorMagnetometer;

    private TextView mTextSensorAzimuth;
    private TextView mTextSensorPitch;
    private TextView mTextSensorRoll;

//    untuk save data dari accele dan magnetometer
    private float[] mAccelerometerData = new float[3];
    private float[] mMagnetometerData = new float[3];

    private static final float VALUE_DRIFT = 0.05f;
    private ImageView mSpotTop;
    private ImageView mSpotBottom;
    private ImageView mSpotRight;
    private ImageView mSpotLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mTextSensorAzimuth = findViewById(R.id.value_azimuth);
        mTextSensorPitch = findViewById(R.id.value_pitch);
        mTextSensorRoll = findViewById(R.id.value_roll);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mSpotTop = findViewById(R.id.spot_top);
        mSpotBottom = findViewById(R.id.spot_bottom);
        mSpotLeft = findViewById(R.id.spot_left);
        mSpotRight = findViewById(R.id.spot_right);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mSensorAccelerometer != null) {
            mSensorManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorMagnetometer != null) {
            mSensorManager.registerListener(this, mSensorMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int sensorType = sensorEvent.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                mAccelerometerData = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagnetometerData = sensorEvent.values.clone();
                break;
            default:
                return;
        }

//        Membuat fungsi untuk mengenerate angka data menjadi rotationmatrix.
//        Fungsinya adalah mentranslate koordinat relatif dari sebuah device
//        menjadi relative terhadap bumi. untuk mendetect position.
        float [] rotationMatrix = new float[9];
        boolean rotationOK = SensorManager.getRotationMatrix(rotationMatrix, null,
                mAccelerometerData, mMagnetometerData);
//        perlu convert lagi untuk menjadi value azimuth, dll menjadi array 3 slot
        float orientationValues[] = new float[3];
        if (rotationOK) {
            SensorManager.getOrientation(rotationMatrix, orientationValues);
        }

        float azimuth = orientationValues[0];
        float pitch = orientationValues[1];
        float roll = orientationValues[2];

//        tampilkan data
        mTextSensorAzimuth.setText(getResources().getString(R.string.value_format, azimuth));
        mTextSensorPitch.setText(getResources().getString(R.string.value_format, pitch));
        mTextSensorRoll.setText(getResources().getString(R.string.value_format, roll));

        if (Math.abs(pitch) < VALUE_DRIFT) {
            pitch = 0;
        }
        if (Math.abs(roll) < VALUE_DRIFT) {
            roll = 0;
        }

        mSpotTop.setAlpha(0f);
        mSpotLeft.setAlpha(0f);
        mSpotRight.setAlpha(0f);
        mSpotBottom.setAlpha(0f);

//        kalau ada perubahan pada pitchnya angkanya dinaikkan sesuai value
        if (pitch > 0){
            mSpotBottom.setAlpha(pitch);
        }
        else {
            mSpotTop.setAlpha(Math.abs(pitch));
        }
        if (roll > 0){
            mSpotLeft.setAlpha(roll);
        }
        else {
            mSpotRight.setAlpha(Math.abs(roll));
        }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}