package com.liziwl.senseflip.ui.testBoard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        testBoardViewModel =
                ViewModelProviders.of(this).get(TestBoardViewModel.class);
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
        bt_doVerify.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!isRunning) {
                    do_start();
                    isRunning = true;
                    Log.d("JSON", "send json");
                    sendPost();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            do_stop(getRandomBoolean());
                        }
                    }, 1000); // 延时1秒
                }

            }
        });


        return root;
    }

    public boolean getRandomBoolean() {
        Random random = new Random();
        return random.nextBoolean();
    }

    public void do_start() {
        testStatus.setText(R.string.tesing_running);
        testStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.rec));
        testStatus.setTextColor(Color.parseColor("#FFFFFF"));
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
            tvValid.setTextColor(Color.parseColor("#FFFFFF"));
            tvValid.setBackgroundColor(ContextCompat.getColor(context, R.color.good));
            tvValid.setText(R.string.tesing_good);
        } else {
            tvValid.setTextColor(ContextCompat.getColor(context, R.color.black));
            tvValid.setBackgroundColor(ContextCompat.getColor(context, R.color.bad));
            tvValid.setText(R.string.tesing_bad);
        }
        isRunning = false;
    }

    public void sendPost() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(JUDGE_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setConnectTimeout(5 * 1000); // millisecond
                    conn.setReadTimeout(5 * 1000); // millisecond

                    JSONObject req_json = new JSONObject();
                    req_json.put("timestamp", 1488873360);

                    Log.i("JSON", req_json.toString());
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

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
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
}
