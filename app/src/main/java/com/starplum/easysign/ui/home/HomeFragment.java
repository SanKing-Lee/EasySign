package com.starplum.easysign.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.util.Properties;

public class HomeFragment extends Fragment {

    // message type
    private final int MSG_TIMER = 1;
    private final int MSG_RST = 2;

    // sign flag
    private final int SIGN_IN_FLAG = 1;
    private final int SIGN_OUT_FLAG = 2;
    private final int WORK_FLAG = 3;

    // property file
    private final String propSignTimeFileName = "sign_time.properties";
    private final Properties propSignTime = new Properties();

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
    private int signInHour;
    private int signInMinute;

    // sign out
    private Button mBtnSignOut;
    private TextView mTextSignOutTime;
    private boolean bSignedOut = false;
    private int signOutHour;
    private int signOutMinute;

    // work time
    private TextView mTextWorkTime;
    private int workHour;
    private int workMinute;

    // reset
    private Button mBtnRst;

    private int getHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    private int getMinute() {
        return Calendar.getInstance().get(Calendar.MINUTE);
    }

    private String buildTime(int hour, int minute) {
        StringBuilder timeBuilder = new StringBuilder();
        if(hour < 10) timeBuilder.append("0");
        timeBuilder.append(hour);
        timeBuilder.append(":");
        if (minute < 10) timeBuilder.append("0");
        timeBuilder.append(minute);
        return timeBuilder.toString();
    }

    private void changeTime(int hour, int minute, int signFlag) {
        switch (signFlag) {
            case SIGN_IN_FLAG:
                signInHour = hour;
                signInMinute = minute;
                mTextSignInTime.setText(buildTime(hour, minute));
                break;
            case SIGN_OUT_FLAG:
                signOutHour = hour;
                signOutMinute = minute;
                mTextSignOutTime.setText(buildTime(hour, minute));
                break;
            case WORK_FLAG:
                workHour = hour;
                workMinute = minute;
                mTextWorkTime.setText(buildTime(hour, minute));
                break;
            default:
                break;
        }
        saveSignTime(signFlag);
    }

    private void updateTime(int signFlag) {
        switch (signFlag) {
            case SIGN_IN_FLAG:
                changeTime(signInHour, signInMinute, signFlag);
                break;
            case SIGN_OUT_FLAG:
                changeTime(signOutHour, signOutMinute, signFlag);
                break;
            case WORK_FLAG:
                changeTime(workHour, workMinute, signFlag);
                break;
            default:
                break;
        }
    }

    private void calWorkTime() {
        int totalWorkMinutes = signOutHour * 60 - signInHour * 60 + signOutMinute - signInMinute;
        if (totalWorkMinutes < 0) {
            workMinute = workHour = 0;
            return;
        }
        workMinute = totalWorkMinutes % 60;
        workHour = totalWorkMinutes / 60;
        changeTime(workHour, workMinute, WORK_FLAG);
    }

    private void addOnClickSignTime() {
        // 在已签到的情况下可以修改签到时间
        TimePickerDialog.OnTimeSetListener signInTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                changeTime(hourOfDay, minute, SIGN_IN_FLAG);
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
        TimePickerDialog.OnTimeSetListener signOutTimeSetListener =
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        changeTime(hourOfDay, minute, SIGN_OUT_FLAG);
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

                        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                        int minute = Calendar.getInstance().get(Calendar.MINUTE);

                        // 更新时间
                        if (!bSignedIn) {
                            mBtnSignIn.setEnabled(true);
                            mBtnSignIn.setText(R.string.sign_in_btn);
                            changeTime(hour, minute, SIGN_IN_FLAG);
                        } else {
                            mBtnSignIn.setEnabled(false);
                            mBtnSignIn.setText(R.string.signed_in_btn);
                        }
                        if (!bSignedOut) {
                            mBtnSignOut.setEnabled(true);
                            mBtnSignOut.setText(R.string.sign_out_btn);
                            changeTime(hour, minute, SIGN_OUT_FLAG);
                        } else {
                            mBtnSignOut.setEnabled(false);
                            mBtnSignOut.setText(R.string.signed_out_btn);
                        }
                        calWorkTime();
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
        mBtnSignIn.setText((bSignedIn)?R.string.signed_in_btn:R.string.sign_in_btn);
        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bSignedIn = true;
                saveSignTime(SIGN_IN_FLAG);
            }
        });

        // 下班签退
        mBtnSignOut.setText((bSignedOut)?R.string.signed_out_btn:R.string.sign_out_btn);
        mBtnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bSignedOut = true;
                saveSignTime(SIGN_OUT_FLAG);
            }
        });

        // 重置
        mBtnRst.setText("重置");
        mBtnRst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bSignedIn = false;
                bSignedOut = false;
            }
        });
    }

    private void loadSignTime() {
        SharedPreferences sharedSignTime = root.getContext().getSharedPreferences("data", 0);
        bSignedIn = sharedSignTime.getBoolean("is_signed_in", false);
        if (bSignedIn) {
            signInHour = sharedSignTime.getInt("sign_in_hour", getHour());
            signInMinute = sharedSignTime.getInt("sign_in_minute", getMinute());
            updateTime(SIGN_IN_FLAG);
        }
        bSignedOut = sharedSignTime.getBoolean("is_signed_out", false);
        if (bSignedOut) {
            signOutHour = sharedSignTime.getInt("sign_out_hour", getHour());
            signOutMinute = sharedSignTime.getInt("sign_out_minute", getMinute());
            updateTime(SIGN_OUT_FLAG);
        }
    }

    private void saveSignTime(int signFlag) {
        SharedPreferences.Editor sharedEditSignTime = root.getContext().getSharedPreferences("data", 0).edit();
        switch (signFlag) {
            case SIGN_IN_FLAG:
                sharedEditSignTime.putBoolean("is_signed_in", bSignedIn);
                sharedEditSignTime.putInt("sign_in_hour", signInHour);
                sharedEditSignTime.putInt("sign_in_minute", signInMinute);
                break;
            case SIGN_OUT_FLAG:
                sharedEditSignTime.putBoolean("is_signed_out", bSignedOut);
                sharedEditSignTime.putInt("sign_out_hour", signOutHour);
                sharedEditSignTime.putInt("sign_out_minute", signOutMinute);
                break;
            default:
                break;
        }
        sharedEditSignTime.apply();
    }

    private void init(LayoutInflater inflater, ViewGroup container) {
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
        // 获取对应的组件
        mTextCurrDate = root.findViewById(R.id.text_current_date);
        mTextCurrTime = root.findViewById(R.id.text_current_time);
        mTextSignInTime = root.findViewById(R.id.text_sign_in_time);
        mTextSignOutTime = root.findViewById(R.id.text_sign_out_time);
        mTextWorkTime = root.findViewById(R.id.text_work_time);

        // 设置各个组件的字体
//        Typeface tfSong = Typeface.createFromAsset(root.getContext().getAssets(), "fonts/song.otf");
//        mTextWorkTime.setTypeface(tfSong);
//        mTextSignOutTime.setTypeface(tfSong);
//        mTextSignInTime.setTypeface(tfSong);

        mBtnSignIn = root.findViewById(R.id.btn_sign_in);
        mBtnSignOut = root.findViewById(R.id.btn_sign_out);
        mBtnRst = root.findViewById(R.id.btn_sign_reset);

        loadSignTime();

        // 向button上添加点击事件
        initButton();

        // 向签到、签退时间显示上添加点击事件
        addOnClickSignTime();

        // 创建消息处理器
        createHandler();

        // 创建计时器
        createTimer();


    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        init(inflater, container);

        return root;
    }
}