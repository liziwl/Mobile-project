package com.liziwl.senseflip.ui.testBoard;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;

import com.liziwl.senseflip.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestBoardFragment extends Fragment {

    private static final String[] PERMISSIONS_INTERNET = {"android.permission.INTERNET"};
    private static final int REQUEST_CODE_INTERNET = 1;
    private TestBoardViewModel testBoardViewModel;
    final String JUDGE_URL = "http://10.20.20.156:5000/judge";

    private Button start_btn; // 开始记录按钮


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
        start_btn = root.findViewById(R.id.start2);
        start_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d("JSON", "send json");
                sendPost();
            }
        });

        return root;
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

                    JSONObject req_json = new JSONObject();
                    req_json.put("timestamp", 1488873360);

                    Log.i("JSON", req_json.toString());
                    DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
                    outputStream.writeBytes(req_json.toString());

                    outputStream.flush();
                    outputStream.close();

                    StringBuilder response_json_str = new StringBuilder();
                    String line;
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
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
