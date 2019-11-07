package com.starplum.easysign.ui.home;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.starplum.easysign.R;

import java.text.DateFormat;

public class HomeFragment extends Fragment {

    // message type
    private final int MSG_TIMER = 1;
    private final int MSG_RST = 2;

    private HomeViewModel homeViewModel;
    private View root;
    private TextView mTextCurrTime;
    private String mStrCurrTime;
    private Handler mHandler;

    private Button mBtnSignIn;
    private TextView mTextSignInTime;
    private boolean bSignedIn = false;

    private Button mBtnSignOut;
    private TextView mTextSignOutTime;
    private boolean bSignedOut = false;

    private Button mBtnRst;

    @SuppressLint("HandlerLeak")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        // 实时显示当前的时间
        mTextCurrTime = root.findViewById(R.id.text_current_time);
        mTextSignInTime = root.findViewById(R.id.text_sign_in_time);
        mTextSignOutTime = root.findViewById(R.id.text_sign_out_time);

        mHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case MSG_TIMER:
                        DateFormat dateFormat = DateFormat.getTimeInstance();
                        mStrCurrTime = dateFormat.format(System.currentTimeMillis());
                        mTextCurrTime.setText(mStrCurrTime);
                        if(!bSignedIn) {
                            mTextSignInTime.setText(mStrCurrTime);
                        }
                        if(!bSignedOut) {
                            mTextSignOutTime.setText(mStrCurrTime);
                        }
                        break;
                    case MSG_RST:
                        mBtnSignIn.setText("签到");
                        bSignedIn = false;
                        mBtnSignOut.setText("签退");
                        bSignedOut = false;
                        break;
                    default:
                        break;
                }
            }
        };

        Runnable Timer = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Message msg = new Message();
                        msg.what = MSG_TIMER;
                        mHandler.sendMessage(msg);
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread t = new Thread(Timer);
        t.start();

        // 上班签到
        mBtnSignIn = root.findViewById(R.id.btn_sign_in);
        mBtnSignIn.setText("签到");
        mBtnSignIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mBtnSignIn.setText("已签到");
                bSignedIn = true;
            }
        });

        // 下班签退
        mBtnSignOut = root.findViewById(R.id.btn_sign_out);
        mBtnSignOut.setText("签退");
        mBtnSignOut.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mBtnSignOut.setText("已签退");
                bSignedOut = true;
            }
        });

        // 重置
        mBtnRst = root.findViewById(R.id.btn_sign_reset);
        mBtnRst.setText("重置");
        mBtnRst.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.what = MSG_RST;
                mHandler.sendMessage(msg);
            }
        });
        return root;
    }
}