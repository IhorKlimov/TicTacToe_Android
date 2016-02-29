package com.example.igorklimov.tictactoe.wifi;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.igorklimov.tictactoe.R;
import com.example.igorklimov.tictactoe.databinding.FragmentRegisterBinding;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import static com.example.igorklimov.tictactoe.wifi.ConnectActivity.CONNECT;

public class RegisterFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private FragmentRegisterBinding mBinding;

    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_register, container, false);
        mBinding.connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });
        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void connect() {
        String name = mBinding.editTextName.getText().toString();
        Utils.hideKeyboard(getContext());
        mBinding.connect.setClickable(false);
        mBinding.progressBar.setVisibility(View.VISIBLE);
        new GcmRegistrationAsyncTask(getContext())
                .execute(name);
    }

    private class GcmRegistrationAsyncTask extends AsyncTask<String, Void, Void> {
        private static final String LOG_TAG = "GcmRegAsyncTask";
        private GoogleCloudMessaging gcm;
        private Context context;

        private static final String SENDER_ID = "1082514991482";

        public GcmRegistrationAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                String regId = gcm.register(SENDER_ID);
                PreferenceManager.getDefaultSharedPreferences(context)
                        .edit()
                        .putString(context.getString(R.string.pref_user_id), regId)
                        .putString(context.getString(R.string.pref_user_name), params[0])
                        .commit();
                Utils.sUserId = regId;
                Utils.sUserName = params[0];
                Utils.sRegService.register(regId, params[0]).execute();
            } catch (IOException ex) {
                ex.printStackTrace();
                String msg = "Error: " + ex.getMessage();
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mListener.onFragmentInteraction(CONNECT);
        }
    }

}
