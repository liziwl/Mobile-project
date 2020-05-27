package com.liziwl.senseflip.ui.testBoard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;

import com.liziwl.senseflip.R;

public class TestBoardFragment extends Fragment {

    private TestBoardViewModel testBoardViewModel;

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
        return root;
    }
}
