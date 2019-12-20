package com.starplum.easysign.ui.home;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.starplum.easysign.MainActivity;
import com.starplum.easysign.R;
import com.starplum.easysign.dao.SignInfoDao;
import com.starplum.easysign.database.ESDBHelper;
import com.starplum.easysign.database.ESDBSchema;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.starplum.easysign.database.ESDBSchema;
import com.starplum.easysign.model.SignInfo;

public class HomeFragment extends Fragment {

    private static final String LOG_TAG = "HomeFragment";

    // message type
    private final int MSG_TIMER = 1;
    private final int MSG_RST = 2;

    // sign flag
    private final int SIGN_IN_FLAG = 1;
    private final int SIGN_OUT_FLAG = 2;
    private final int WORK_FLAG = 3;

    private final static SimpleDateFormat SIGN_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
    private final static long WORK_FULFILL_MS = 8 * 60 * 60 * 1000;

    private String currDateStamp;

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

    private SignInfo mSignInfo;

    // sign in
    private Button mBtnSignIn;
    private TextView mTextSignInTime;

    // sign out
    private Button mBtnSignOut;
    private TextView mTextSignOutTime;

    // work time
    private TextView mTextWorkTime;

    // reset
    private Button mBtnRst;

    // database
    private SignInfoDao mSignInfoDao;

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
        // 获取对应的组件
        mTextCurrDate = root.findViewById(R.id.text_current_date);
        mTextCurrTime = root.findViewById(R.id.text_current_time);
        mTextSignInTime = root.findViewById(R.id.text_sign_in_time);
        mTextSignOutTime = root.findViewById(R.id.text_sign_out_time);
        mTextWorkTime = root.findViewById(R.id.text_work_time);

        mBtnSignIn = root.findViewById(R.id.btn_sign_in);
        mBtnSignOut = root.findViewById(R.id.btn_sign_out);
        mBtnRst = root.findViewById(R.id.btn_sign_reset);

        mSignInfoDao = new SignInfoDao(getContext());

        currDateStamp = SignInfo.DATE_STAMP_FORMAT.format(System.currentTimeMillis());

        mSignInfo = mSignInfoDao.loadSignInfo(currDateStamp);

        mSignInfo.setWorkFulfillMs(WORK_FULFILL_MS);

        // 向button上添加点击事件
        initButton();

        // 向签到、签退时间显示上添加点击事件
        addOnClickSignTime();

        // 创建消息处理器
        createHandler();

        // 创建计时器
        createTimer();

        return root;
    }

    private String formatMsToTime(long ms) {
        return SIGN_TIME_FORMAT.format(ms);
    }

    private void updateCurrentTime() {
        // 当前的日期
        DateFormat dateFormat = DateFormat.getDateInstance();
        mStrCurrDate = dateFormat.format(System.currentTimeMillis());
        mTextCurrDate.setText(mStrCurrDate);

        // 当前的时间
        DateFormat timeFormat = DateFormat.getTimeInstance();
        mStrCurrTime = timeFormat.format(System.currentTimeMillis());
        mTextCurrTime.setText(mStrCurrTime);
    }

    private void updateSignInfo() {
        // 更新时间
        if (!mSignInfo.isSignedIn()) {
            mBtnSignIn.setEnabled(true);
            mBtnSignIn.setText(R.string.sign_in_btn);
            changeTime(SIGN_IN_FLAG, System.currentTimeMillis());
        } else {
            mBtnSignIn.setEnabled(false);
            mBtnSignIn.setText(R.string.signed_in_btn);
        }
        if (!mSignInfo.isSignedOut()) {
            mBtnSignOut.setEnabled(true);
            mBtnSignOut.setText(R.string.sign_out_btn);
            changeTime(SIGN_OUT_FLAG, System.currentTimeMillis());
        } else {
            mBtnSignOut.setEnabled(false);
            mBtnSignOut.setText(R.string.signed_out_btn);
        }
        mTextSignInTime.setText(formatMsToTime(mSignInfo.getSignInMs()));
        mTextSignOutTime.setText(formatMsToTime(mSignInfo.getSignOutMs()));

        mSignInfo.calWorkMs();
        mSignInfo.setWorkFulfill();
        mTextWorkTime.setText(formatMsToTime(mSignInfo.getWorkMs()));
    }

    private void changeTime(int flag, long ms) {
        switch (flag) {
            case SIGN_IN_FLAG:
                mSignInfo.setSignInMs(ms);
                break;
            case SIGN_OUT_FLAG:
                mSignInfo.setSignOutMs(ms);
                break;
            case WORK_FLAG:
                mSignInfo.setWorkMs(ms);
                break;
            default:
                break;
        }
    }

    private void hmToMs(int hour, int minute, int signFlag) {
        int currHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int currMinute = Calendar.getInstance().get(Calendar.MINUTE);
        int currSec = Calendar.getInstance().get(Calendar.SECOND);
        long ms = System.currentTimeMillis() - (currHour - hour) * 60 * 60 * 1000
                - (currMinute - minute) * 60 * 1000
                - currSec * 1000;
        changeTime(signFlag, ms);
        mSignInfoDao.updateSignInfo(mSignInfo);
    }

    private void addOnClickSignTime() {
        // 在已签到的情况下可以修改签到时间
        TimePickerDialog.OnTimeSetListener signInTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                hmToMs(hourOfDay, minute, SIGN_IN_FLAG);
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
                if (!mSignInfo.isSignedIn()) {
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
                        hmToMs(hourOfDay, minute, SIGN_OUT_FLAG);
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
                if (!mSignInfo.isSignedOut()) {
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
                        updateCurrentTime();
                        updateSignInfo();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void createTimer() {
        // 计时器线程，每过0.1秒发送消息，更新当前的日期和时间
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
        mBtnSignIn.setText((mSignInfo.isSignedIn()) ? R.string.signed_in_btn : R.string.sign_in_btn);
        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignInfo.setSignedIn(true);
            }
        });

        // 下班签退
        mBtnSignOut.setText((mSignInfo.isSignedOut()) ? R.string.signed_out_btn : R.string.sign_out_btn);
        mBtnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignInfo.setSignedOut(true);
            }
        });

        // 重置
        mBtnRst.setText("重置");
        mBtnRst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignInfo.setSignedIn(false);
                mSignInfo.setSignedOut(false);
            }
        });
    }

    @Override
    public void onPause() {
        Log.i(LOG_TAG, "onPause");
        super.onPause();
        mSignInfoDao.updateSignInfo(mSignInfo);
    }
}