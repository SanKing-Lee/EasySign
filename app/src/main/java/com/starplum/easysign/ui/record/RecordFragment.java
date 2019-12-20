package com.starplum.easysign.ui.record;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.starplum.easysign.R;
import com.starplum.easysign.dao.SignInfoDao;
import com.starplum.easysign.model.SignInfo;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RecordFragment extends Fragment {

    private static final String LOG_TAG = "RecordFragment";
    private RecordViewModel galleryViewModel;

    private RecyclerView mRecyclerView;
    private SignInfoAdapter mSignInfoAdapter;

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

    private class SignInfoAdapter extends RecyclerView.Adapter<SignInfoAdapter.SignInfoHolder> {

        private List<SignInfo> mSignInfos;

        public SignInfoAdapter(List<SignInfo> signInfos) {
            mSignInfos = signInfos;
        }

        @NonNull
        @Override
        public SignInfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new SignInfoHolder(inflater.inflate(R.layout.sign_info_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull SignInfoHolder holder, int position) {
            SignInfo signInfo = mSignInfos.get(position);
            holder.bind(signInfo);
        }

        @Override
        public int getItemCount() {
            return mSignInfos.size();
        }

        private class SignInfoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private TextView tvSignInTime;
            private TextView tvSignOutTime;
            private TextView tvSignDate;
            private TextView tvWorkTime;
            private CheckBox cbWorkFulfill;

            private String formatMsTime(Long ms) {
                return TIME_FORMAT.format(ms);
            }

            public SignInfoHolder(@NonNull View itemView) {
                super(itemView);
                tvSignInTime = itemView.findViewById(R.id.text_record_sign_in_time);
                tvSignOutTime = itemView.findViewById(R.id.text_record_sign_out_time);
                tvSignDate = itemView.findViewById(R.id.text_record_sign_date);
                tvWorkTime = itemView.findViewById(R.id.text_record_work_time);
                cbWorkFulfill = itemView.findViewById(R.id.checkBox);
                cbWorkFulfill.setEnabled(false);
            }

            public void bind(SignInfo signInfo) {
                Log.i(LOG_TAG, "Binding sign info: " + signInfo.toString());
                tvSignInTime.setText(formatMsTime(signInfo.getSignInMs()));
                tvSignOutTime.setText(formatMsTime(signInfo.getSignOutMs()));
                tvSignDate.setText(DateFormat.getDateInstance().format(signInfo.getSignOutMs()));
                tvWorkTime.setText(signInfo.buildWorkTime());
                cbWorkFulfill.setChecked(signInfo.isWorkFulfill());
            }

            @Override
            public void onClick(View v) {

            }
        }

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(RecordViewModel.class);
        View root = inflater.inflate(R.layout.fragment_record, container, false);
        mRecyclerView = root.findViewById(R.id.sign_info_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return root;
    }

    private void updateUI() {
        SignInfoDao signInfoDao = new SignInfoDao(getContext());
        List<SignInfo> signInfos = signInfoDao.getFinishedSignInfos();
        if(mSignInfoAdapter == null) {
            mSignInfoAdapter = new SignInfoAdapter(signInfos);
            mRecyclerView.setAdapter(mSignInfoAdapter);
        }
        else {
            mSignInfoAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }
}