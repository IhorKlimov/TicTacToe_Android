package com.example.igorklimov.tictactoe.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.example.igorklimov.tictactoe.R;
import com.example.igorklimov.tictactoe.databinding.ActivityConnectBinding;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.iklimov.tictactoe.backend.game.model.Player;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectActivity extends AppCompatActivity
        implements OnFragmentInteractionListener {
    private static final String LOG_TAG = "ConnectActivity";
    private boolean mIsPlayerList;

    public static final int CONNECT = 1;
    public static final int START = 2;

    private BroadcastReceiver mReceiver;
    private Context mContext;
    private FragmentManager mSupportFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityConnectBinding mBinding = DataBindingUtil
                .setContentView(this, R.layout.activity_connect);

        mContext = this;

        mSupportFragmentManager = getSupportFragmentManager();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userId = preferences
                .getString(getString(R.string.pref_user_id), "");
        String name = preferences.getString(getString(R.string.pref_user_name), "");

        if (userId.equals("")) {
            mSupportFragmentManager.beginTransaction()
                    .replace(R.id.container, new RegisterFragment())
                    .commit();
        } else {
            Utils.sUserId = userId;
            Utils.sUserName = name;
            mIsPlayerList = true;
            mSupportFragmentManager.beginTransaction()
                    .replace(R.id.container, new PlayersListFragment())
                    .commit();
            new SignUp().execute(true);
        }

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(mContext);
                // The getMessageType() intent parameter must be the intent you received
                // in your BroadcastReceiver.
                String messageType = gcm.getMessageType(intent);

                if (extras != null && !extras.isEmpty()) {  // has effect of unparcelling Bundle
                    // Since we're not using two way messaging, this is all we really to check for
                    if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                        Logger.getLogger("GCM_RECEIVED").log(Level.INFO, extras.toString());

                        String action = extras.getString(Utils.ACTION);

                        if (action == null) {
                            return;
                        }
                        Player player = new Player();
                        player.setName(extras.getString(Utils.NAME));
                        player.setRegId(extras.getString(Utils.REG_ID));

                        if (action.equals(Utils.INVITE_TO_PLAY)) {
                            showInviteDialog(player);
                        } else if (action.equals(Utils.NEW_USER)) {
                            addNewPlayer(player);
                        } else if (action.equals(Utils.START_GAME)) {
                            Utils.startGame(mContext, extras);
                        }
                    }
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, new IntentFilter("com.google.android.c2dm.intent.RECEIVE"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        new SignUp().execute(false);
    }

    @Override
    public void onFragmentInteraction(int action) {
        switch (action) {
            case CONNECT:
                mIsPlayerList = true;
                mSupportFragmentManager.beginTransaction()
                        .replace(R.id.container, new PlayersListFragment())
                        .commit();
                break;
            case START:

                break;
        }
    }

    private void addNewPlayer(Player player) {
        if (!mIsPlayerList) {
            return;
        }

        PlayersListFragment fr = (PlayersListFragment)
                mSupportFragmentManager.findFragmentById(R.id.container);
        if (fr.adapter.players!=null) {
            fr.adapter.players.add(player);
            fr.adapter.notifyDataSetChanged();
        }
    }

    private void showInviteDialog(Player player) {
        final String opponentsId = player.getRegId();

        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.format_invite_to_play, player.getName()))
                .setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Utils.SendResponse(true, false).execute(Utils.sUserId, opponentsId);
                    }
                })
                .setNegativeButton(getString(R.string.deny), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Utils.SendResponse(false, false).execute(Utils.sUserId, opponentsId);
                    }
                })
                .show();
    }

    private class SignUp extends AsyncTask<Boolean, Void, Void> {
        @Override
        protected Void doInBackground(Boolean... params) {
            if (Utils.sUserId == null || Utils.sUserId.equals("")) {
                return null;
            }
            try {
                if (params[0]) {
                    Utils.sRegService.signUpForGame(Utils.sUserId, Utils.sUserName).execute();
                } else {
                    Utils.sRegService.unSignUpFromGame(Utils.sUserId).execute();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
