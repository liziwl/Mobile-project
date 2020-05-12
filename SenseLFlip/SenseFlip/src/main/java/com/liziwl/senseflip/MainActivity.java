package com.liziwl.senseflip;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import java.util.PriorityQueue;

public class MainActivity extends AppCompatActivity {
    SensorManager mSensorManager = null;
    AccelerometerSilentListener mAccelerometerSilentListener = null;
    private TextView tvSampleFileName; // 文件名样例显示框
    private EditText editText; // 文件名前缀输入框
    private Button set_prefix; // 设置文件名前缀
    private String prefix; // 文件名前缀存储
    private String filename_now; // 当前文件名


    private TextView status_tv; // 状态文字显示
    private boolean isRunning; // 状态记录
    private Button start_btn; // 开始记录按钮
    private Button end_btn; // 中止记录按钮

    private TextView log_tv; // 历史文件名log显示

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
        tvSampleFileName = findViewById(R.id.tv_sample_filename);
        verifyStoragePermissions(this);


        filp_times = 0;

        // 注册传感器
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccelerometerSilentListener = new AccelerometerSilentListener();
        mSensorManager.registerListener(mAccelerometerSilentListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);


        // tvSampleFileName.setText(String.format("翻转次数: %d", filp_times));
        // SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
        // Date date = new Date(System.currentTimeMillis());
        // util.writeToFile(
        //         String.format("%s: %s", simpleDateFormat.format(date), tvSampleFileName.getText().toString()),
        //         DEFAULT_FILENAME);

        set_prefix = findViewById(R.id.update_filename);
        editText = findViewById(R.id.filename_prefix);

        set_prefix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String _prefix = editText.getText().toString();
                updatePrefix(_prefix);
            }
        });
        set_prefix.performClick();


        status_tv = findViewById(R.id.status);
        start_btn = findViewById(R.id.start);
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRunning) {
                    filename_now = util.fileNameFormater(prefix);
                    status_tv.setText(R.string.running);
                    addLog(filename_now);
                    isRunning = true;
                }
            }
        });

        end_btn = findViewById(R.id.end);
        end_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status_tv.setText(R.string.stopped);
                isRunning = false;
            }
        });
        end_btn.performClick();

        log_tv = findViewById(R.id.log_filename);
    }

    public void addLog(String line) {
        String old = log_tv.getText().toString();
        old = line + "\n" + old;
        log_tv.setText(old);
    }

    protected void updatePrefix(String new_prefix) {
        new_prefix = new_prefix.trim();
        String filename = new_prefix + "_%time" + ".csv";
        tvSampleFileName.setText(filename);
        prefix = new_prefix;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mAccelerometerSilentListener);
        }
    }


    private class AccelerometerSilentListener implements SensorEventListener {
        // private static final int SPEED_LIMIT = 800; //速度阀指
        // private static final int UPDATE_INTERNAL_TIME = 60; //两次取样时间差
        // private long lastTime = 0;
        //
        // private float lastX = 0;
        // private float lastY = 0;
        // private float lastZ = 0;
        //
        // private boolean isUp = true;
        // private int queueSize;
        private PriorityQueue<XYZ> dataList = new PriorityQueue<>(200,new XYZComparator());

        @Override
        public void onSensorChanged(SensorEvent event) {
            //  long currentTime = System.currentTimeMillis();
            //  float internalTime = currentTime - lastTime;
            //  if (internalTime < UPDATE_INTERNAL_TIME) {
            //      return;
            //  }
            //  lastTime = currentTime;
            //
            //  SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
            //  Date date = new Date(System.currentTimeMillis());
            // // time1.setText("Date获取当前日期时间"+simpleDateFormat.format(date));


            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            long timeStamp = event.timestamp;
            // long timeStamp = event.timestamp + SystemClock.elapsedRealtimeNanos();

            while (dataList.size()>=200){
                // Log.d("SIZE", String.valueOf(dataList.size()));
                dataList.poll();
            }

            // queueSize++;
            XYZ data_tmp = new XYZ(x, y, z, timeStamp);
            dataList.add(data_tmp);

            Log.d("~~~~", data_tmp.toString());

            if (dataList.size()>=2) {
                XYZ head = dataList.peek();
                long period = data_tmp.getDataTimeNano() - head.getDataTimeNano();
                Log.d("~~~~!period", String.valueOf(1.0E-9 * period));
                double rate = dataList.size() / (1.0E-9 * period);
                Log.d("~~~~!rate", String.valueOf(rate));
            }
            //  float deltaX = x - lastX;
            //  float deltaY = y - lastY;


            //  float deltaZ = z - lastZ;
            //  lastX = x;
            //  lastY = y;
            //  lastZ = z;
            //  boolean is_now_up = isUp;
            //
            //
            //  double speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) / internalTime * 10000; //算出后速度为 mm/s
            // // Log.d("!!!", String.format("%b speed%f x%.2f y%.2f z%.2f", isUp, speed, x, y, z));
            //
            //  if (speed > SPEED_LIMIT) {
            //      return;
            //  }
            //
            //  if (x > -2 && x < 2 && y > -2 && y < 2) {
            //      if (z > 0) {
            //          is_now_up = true;
            //      } else {
            //          is_now_up = false;
            //      }
            //  }
            //
            //  if (is_now_up != isUp) {
            //     // Log.d("!!!", String.format("shake x%.2f y%.2f z%.2f", x, y, z));
            //      filp_times++;
            //      // tvSampleFileName.setText(String.format("翻转次数: %d", filp_times));
            //      // isUp = is_now_up;
            //      // util.writeToFile(
            //      //         String.format("%s: %s", simpleDateFormat.format(date), tvSampleFileName.getText().toString()),
            //      //         DEFAULT_FILENAME);
            //  }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    public static void verifyStoragePermissions(Activity activity) {

        try {
            // 检测是否有写的权限
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


}
