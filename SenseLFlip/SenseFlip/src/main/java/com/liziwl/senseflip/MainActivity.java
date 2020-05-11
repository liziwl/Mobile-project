package com.liziwl.senseflip;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    SensorManager mSensorManager = null;
    AccelerometerSilentListener mAccelerometerSilentListener = null;
    private TextView tvSense;
    private int filp_times = 0;

    private static final String DEFAULT_FILENAME = "SenseFlip_log.txt";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvSense = findViewById(R.id.tv_sense);
        verifyStoragePermissions(this);
        Log.d("???", Environment.getExternalStorageDirectory().toString());

        filp_times = 0;

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccelerometerSilentListener = new AccelerometerSilentListener();
        mSensorManager.registerListener(mAccelerometerSilentListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        tvSense.setText(String.format("翻转次数: %d", filp_times));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
        Date date = new Date(System.currentTimeMillis());
        writeToFile(String.format("%s: %s", simpleDateFormat.format(date), tvSense.getText().toString()));

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mAccelerometerSilentListener);
        }
    }


    private class AccelerometerSilentListener implements SensorEventListener {
        private static final int SPEED_LIMIT = 800; //速度阀指
        private static final int UPDATE_INTERNAL_TIME = 60; //两次取样时间差
        private long lastTime = 0;

        private float lastX = 0;
        private float lastY = 0;
        private float lastZ = 0;

        private boolean isUp = true;

        @Override
        public void onSensorChanged(SensorEvent event) {
            long currentTime = System.currentTimeMillis();
            float internalTime = currentTime - lastTime;
            if (internalTime < UPDATE_INTERNAL_TIME) {
                return;
            }
            lastTime = currentTime;

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
            Date date = new Date(System.currentTimeMillis());
//            time1.setText("Date获取当前日期时间"+simpleDateFormat.format(date));


            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float deltaX = x - lastX;
            float deltaY = y - lastY;
            float deltaZ = z - lastZ;
            lastX = x;
            lastY = y;
            lastZ = z;
            boolean is_now_up = isUp;


            double speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) / internalTime * 10000; //算出后速度为 mm/s
//            Log.d("!!!", String.format("%b speed%f x%.2f y%.2f z%.2f", isUp, speed, x, y, z));

            if (speed > SPEED_LIMIT) {
                return;
            }

            if (x > -2 && x < 2 && y > -2 && y < 2) {
                if (z > 0) {
                    is_now_up = true;
                } else {
                    is_now_up = false;
                }
            }

            if (is_now_up != isUp) {
//                Log.d("!!!", String.format("shake x%.2f y%.2f z%.2f", x, y, z));
                filp_times++;
                tvSense.setText(String.format("翻转次数: %d", filp_times));
                isUp = is_now_up;
                writeToFile(String.format("%s: %s", simpleDateFormat.format(date), tvSense.getText().toString()));

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(String context) {
        try {
            File file = new File(Environment.getExternalStorageDirectory(),
                    DEFAULT_FILENAME);
            //第二个参数意义是说是否以append方式添加内容
            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(file, true), "UTF-8"));
            bw.write(context);
            bw.write(System.lineSeparator());
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
