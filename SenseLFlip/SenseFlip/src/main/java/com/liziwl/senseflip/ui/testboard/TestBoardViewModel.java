package com.liziwl.senseflip.ui.testboard;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class TestBoardViewModel extends ViewModel {

    private MutableLiveData<String> authName;

    public TestBoardViewModel() {
        authName = new MutableLiveData<>();
        authName.setValue("default");
    }

    public LiveData<String> getAuthName() {
        return authName;
    }

    public void setAuthName(String name) {
        authName.setValue(name);
    }
}