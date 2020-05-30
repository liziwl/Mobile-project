package com.liziwl.senseflip.ui.testboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;
import android.widget.EditText;
import android.widget.TextView;

import com.liziwl.senseflip.R;
import com.liziwl.senseflip.XYZ;
import com.liziwl.senseflip.XYZComparator;
import com.liziwl.senseflip.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.PriorityQueue;
import java.util.Random;


public class TestBoardFragment extends Fragment {

    private static final String[] PERMISSIONS_INTERNET = {"android.permission.INTERNET"};
    private static final int REQUEST_CODE_INTERNET = 1;
    private TestBoardViewModel testBoardViewModel;
    final String JUDGE_URL = "http://vm.liziwl.cn:5000/judge";
    private Context context;

    private TextView tvAuthName; // 用户名
    private EditText editText; // 文件名前缀输入框
    private Button set_authName; // 开始记录按钮

    private TextView tvValid;
    private TextView testStatus;
    private Button bt_doVerify; // 开始记录按钮
    private Boolean isRunning;
    private Boolean isUploading;

    private TextView sample_rate_tv;
    final int CAPACITY = 1000;
    private PriorityQueue<XYZ> dataList;
    SensorManager mSensorManager = null;
    AccelerometerSilentListener mAccelerometerSilentListener = null;
    final int DO_START = 0;
    final int DO_STOP_GOOD = 1;
    final int DO_STOP_BAD = 2;

    /*构造一个Handler，主要作用有：1）供非UI线程发送Message  2）处理Message并完成UI更新*/
    public Handler uiHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    do_start();
                    break;
                case 1:
                    do_stop(true);
                    break;
                case 2:
                    do_stop(false);
                    break;
                default:
                    break;

            }
            return false;
        }
    });

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        testBoardViewModel = ViewModelProviders.of(this).get(TestBoardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_testboard, container, false);
        // final TextView textView = root.findViewById(R.id.text_dashboard);
        // testBoardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
        //     @Override
        //     public void onChanged(@Nullable String s) {
        //         textView.setText(s);
        //     }
        // });

        verifyNetworkPermissions(getActivity());
        context = getActivity().getApplicationContext();

        tvAuthName = root.findViewById(R.id.tv_testing_username);
        editText = root.findViewById(R.id.et_username);
        set_authName = root.findViewById(R.id.update_username);
        set_authName.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                String new_name = editText.getText().toString().trim();
                testBoardViewModel.setAuthName(new_name);
                tvAuthName.setText("当前测试用户：" + new_name);
            }
        });
        set_authName.performClick();


        tvValid = root.findViewById(R.id.tv_valid);
        testStatus = root.findViewById(R.id.status_collect);
        bt_doVerify = root.findViewById(R.id.start2);
        isRunning = false;
        isUploading = false;
        bt_doVerify.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!isRunning) {
                    uiHandler.sendEmptyMessage(DO_START);
                    dataList.clear();

                    // 强行返回，测试用
                    // new Handler().postDelayed(new Runnable() {
                    //     @Override
                    //     public void run() {
                    //         do_stop(getRandomBoolean());
                    //     }
                    // }, 1000); // 延时1秒
                }

            }
        });


        sample_rate_tv = root.findViewById(R.id.tv_sample_rate2);
        // 注册传感器
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccelerometerSilentListener = new AccelerometerSilentListener();
        mSensorManager.registerListener(mAccelerometerSilentListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        dataList = new PriorityQueue<>(CAPACITY, new XYZComparator());


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mAccelerometerSilentListener);
        }
    }


    public boolean getRandomBoolean() {
        Random random = new Random();
        return random.nextBoolean();
    }

    public void do_start() {
        isRunning = true;
        testStatus.setText(R.string.tesing_running);
        testStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.rec));
        testStatus.setTextColor(ContextCompat.getColor(context, R.color.white));
        testStatus.setTypeface(null, Typeface.BOLD);

        tvValid.setText(R.string.wait_auth);
        tvValid.setBackgroundColor(0);
        tvValid.setTextColor(ContextCompat.getColor(context, android.R.color.tab_indicator_text));
    }

    public void do_stop(Boolean passed) {
        testStatus.setText(R.string.tesing_stopped);
        testStatus.setBackgroundColor(0);
        testStatus.setTextColor(ContextCompat.getColor(context, android.R.color.tab_indicator_text));
        testStatus.setTypeface(null, Typeface.NORMAL);
        if (passed) {
            tvValid.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvValid.setBackgroundColor(ContextCompat.getColor(context, R.color.good));
            tvValid.setText(R.string.tesing_good);
        } else {
            tvValid.setTextColor(ContextCompat.getColor(context, R.color.black));
            tvValid.setBackgroundColor(ContextCompat.getColor(context, R.color.bad));
            tvValid.setText(R.string.tesing_bad);
        }
        isRunning = false;
        isUploading = false;
    }

    public void sendPost(final HttpCallBackListener listener) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(JUDGE_URL);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setConnectTimeout(5 * 1000); // millisecond
                    conn.setReadTimeout(5 * 1000); // millisecond

                    JSONObject req_json_data = util.queue2Json(dataList);
                    JSONObject req_json = new JSONObject();
                    req_json.put("requester", testBoardViewModel.getAuthName());
                    req_json.put("data", req_json_data);
                    Log.i("JSON++", req_json.toString());


                    DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
                    outputStream.writeBytes(req_json.toString());

                    outputStream.flush();
                    outputStream.close();

                    StringBuilder response_json_str = new StringBuilder();
                    String line;
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    while ((line = in.readLine()) != null) {
                        response_json_str.append(line);
                    }
                    // 解析返回json对象
                    JSONObject resp_json = new JSONObject(response_json_str.toString());
                    Log.i("JSON-response", resp_json.toString());
                    Log.i("JSON-STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("JSON-MSG", conn.getResponseMessage());

                    if (listener != null) {
                        //回调onFinish方法
                        listener.onSuccess(resp_json);
                    }

                } catch (Exception e) {
                    if (listener != null) {
                        //回调onError方法
                        listener.onError(e);
                    }
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        });

        thread.start();
    }


    private static void verifyNetworkPermissions(Activity activity) {

        try {
            // 检测是否有网络的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有网络的权限，去申请网络的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_INTERNET, REQUEST_CODE_INTERNET);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

        public boolean tap_detect(@NonNull XYZ data) {
            return Math.pow(Math.abs(data.z - 8), 2) > 100;
        }


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

            // 限制大小
            while (dataList.size() >= CAPACITY) {
                // Log.d("SIZE", String.valueOf(dataList.size()));
                dataList.poll();
            }

            XYZ data_tmp = new XYZ(x, y, z, timeStamp);
            dataList.add(data_tmp);
            // Log.d("~~~~", data_tmp.toString());

            if (dataList.size() >= 2) {
                XYZ head = dataList.peek();
                long period = data_tmp.getTimestamp() - head.getTimestamp();
                // Log.d("~~~~!period", String.valueOf(1.0E-9 * period));
                double rate = dataList.size() / (1.0E-9 * period);
                // Log.d("~~~~!rate", String.valueOf(rate));
                sample_rate_tv.setText(String.format("%.2f Hz", rate));
            }

            if (isRunning && !isUploading && tap_detect(data_tmp)) {
                Log.d("JSON", "tap_detect send json");
                isUploading = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendPost(new HttpCallBackListener() {
                            @Override
                            public void onSuccess(JSONObject respose) {
                                try {
                                    boolean passed = (boolean) respose.get("passed");
                                    if (passed) {
                                        uiHandler.sendEmptyMessage(DO_STOP_GOOD);
                                    } else {
                                        uiHandler.sendEmptyMessage(DO_STOP_BAD);
                                    }
                                } catch (JSONException e) {
                                    uiHandler.sendEmptyMessage(DO_STOP_BAD);
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(Exception e) {
                                uiHandler.sendEmptyMessage(DO_STOP_BAD);
                                e.printStackTrace();
                            }
                        });
                    }
                }, 500); // 延时设置（毫秒）
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

}
