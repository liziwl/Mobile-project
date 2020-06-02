package com.liziwl.senseflip.ui.testboard;

import org.json.JSONObject;

/**
 *
 * 网络请求回调接口
 */
public interface HttpCallBackListener {
    void onSuccess(JSONObject respose);
    void onError(Exception e);
}