package com.starplum.easysign.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.starplum.easysign.R;

import java.text.DateFormat;
import java.util.Calendar;

public class HomeFragment extends Fragment {

    // message type
    private final int MSG_TIMER = 1;
    private final int MSG_RST = 2;

    // view
    private HomeViewModel homeViewModel;
    private View root;

    // current date
    private TextView mTextCurrDate;
    private String mStrCurrDate;

    // current time
    private TextView mTextCurrTime;
    private String mStrCurrTime;

    // message handler
    private Handler mHandler;

    // sign in
    private Button mBtnSignIn;
    private TextView mTextSignInTime;
    private boolean bSignedIn = false;

    // sign out
    private Button mBtnSignOut;
    private TextView mTextSignOutTime;
    private boolean bSignedOut = false;

    // reset
    private Button mBtnRst;

    private String buildTime(int hour, int minute) {
        StringBuilder timeBuilder = new StringBuilder();
        timeBuilder.append(hour);
        timeBuilder.append(":");
        if (minute < 10) timeBuilder.append("0");
        timeBuilder.append(minute);
        return timeBuilder.toString();
    }

    private void displaySignTime() {
        // 实时显示当前的时间
        mTextCurrDate = root.findViewById(R.id.text_current_date);
        mTextCurrTime = root.findViewById(R.id.text_current_time);

        // 在已签到的情况下可以修改签到时间
        mTextSignInTime = root.findViewById(R.id.text_sign_in_time);
        TimePickerDialog.OnTimeSetListener signInTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String time = buildTime(hourOfDay, minute);
                mTextSignInTime.setText(time);
            }
        };
        final TimePickerDialog signInTimePickerDialog =
                new TimePickerDialog(this.getContext(), signInTimeSetListener,
                        Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                        Calendar.getInstance().get(Calendar.MINUTE),
                        true);
        mTextSignInTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bSignedIn) {
                    return;
                }
                signInTimePickerDialog.show();
            }
        });

        // 在已签退的情况下可以修改签退时间
        mTextSignOutTime = root.findViewById(R.id.text_sign_out_time);
        TimePickerDialog.OnTimeSetListener signOutTimeSetListener =
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String time = buildTime(hourOfDay, minute);
                        mTextSignOutTime.setText(time);
                    }
                };
        final TimePickerDialog signOutTimePickerDialog =
                new TimePickerDialog(this.getContext(), signOutTimeSetListener,
                        Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                        Calendar.getInstance().get(Calendar.MINUTE),
                        true);
        mTextSignOutTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bSignedOut) {
                    return;
                }
                signOutTimePickerDialog.show();
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private void createHandler() {
        // 消息处理器，对接收到的消息进行处理
        mHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                // 根据消息类型进行相应的处理：更新时间、更新按钮，重置按钮
                switch (msg.what) {
                    // 日期时间更新
                    case MSG_TIMER:
                        // 当前的日期
                        DateFormat dateFormat = DateFormat.getDateInstance();
                        mStrCurrDate = dateFormat.format(System.currentTimeMillis());
                        mTextCurrDate.setText(mStrCurrDate);

                        // 当前的时间
                        DateFormat timeFormat = DateFormat.getTimeInstance();
                        mStrCurrTime = timeFormat.format(System.currentTimeMillis());
                        mTextCurrTime.setText(mStrCurrTime);

                        String calendarTime =
                                buildTime(Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                                        Calendar.getInstance().get(Calendar.MINUTE));
                        // 更新时间
                        if (!bSignedIn) {
                            mTextSignInTime.setText(calendarTime);
                        }
                        if (!bSignedOut) {
                            mTextSignOutTime.setText(calendarTime);
                        }
                        break;
                    //重置签到、签退按钮
                    case MSG_RST:
                        mBtnSignIn.setText("签到");
                        mBtnSignIn.setActivated(true);
                        bSignedIn = false;
                        mBtnSignOut.setText("签退");
                        mBtnSignOut.setActivated(true);
                        bSignedOut = false;
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void createTimer() {
        // 计时器线程，每过一秒发送消息，更新当前的日期和时间
        Runnable Timer = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Message msg = new Message();
                        msg.what = MSG_TIMER;
                        mHandler.sendMessage(msg);
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread t = new Thread(Timer);
        t.start();
    }

    private void initButton() {
        // 上班签到
        mBtnSignIn = root.findViewById(R.id.btn_sign_in);
        mBtnSignIn.setText("签到");
        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnSignIn.setText("已签到");
                mBtnSignIn.setActivated(false);
                bSignedIn = true;
            }
        });

        // 下班签退
        mBtnSignOut = root.findViewById(R.id.btn_sign_out);
        mBtnSignOut.setText("签退");
        mBtnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnSignOut.setText("已签退");
                mBtnSignOut.setActivated(false);
                bSignedOut = true;
            }
        });

        // 重置
        mBtnRst = root.findViewById(R.id.btn_sign_reset);
        mBtnRst.setText("重置");
        mBtnRst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.what = MSG_RST;
                mHandler.sendMessage(msg);
            }
        });
    }

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

        createHandler();

        createTimer();

        displaySignTime();

        initButton();

        return root;
    }


}