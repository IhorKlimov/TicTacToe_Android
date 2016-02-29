/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.igorklimov.tictactoe.wifi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.InputMethodManager;

import com.example.igorklimov.tictactoe.MainActivity;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.iklimov.tictactoe.backend.game.Game;

import java.io.IOException;

/**
 * Created by Igor Klimov on 2/27/2016.
 */
public class Utils {
    public static final int NOTIFICATION_ID = 1;
    public static final String ACTION = "action";
    public static final String NAME = "name";
    public static final String REG_ID = "regId";
    public static final String ROW = "Row";
    public static final String COL = "Col";
    public static final String YOU_FIRST = "you first";
    public static final String IS_WIFI_GAME = "Is Wi-Fi Game";
    public static final String OPPONENTS_REG_ID = "Opponents RegId";
    public static final String SIDE = "side";
    public static final String IS_ORGANIZER = "Is Organizer";

    public static final String INVITE_TO_PLAY = "invite to play";
    public static final String NEW_USER = "new user";
    public static final String START_GAME = "start game";
    public static final String TURN = "turn";
    public static final String X = "X";
    public static final String O = "O";
    public static final String IS_OFFLINE = "Is Offline";



    public static Game sRegService = null;
    public static String sUserId;
    public static String sUserName;

    static {
        Game.Builder builder = new Game.Builder(
                AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                .setRootUrl("https://tictactoe-1233.appspot.com/_ah/api/");
        // end of optional local run code

        sRegService = builder.build();
    }

    public static void startGame(Context context, Bundle extras) {
        Intent intent = new Intent(context, MainActivity.class)
                .putExtra(Utils.IS_WIFI_GAME, true)
                .putExtra(Utils.YOU_FIRST, Boolean.valueOf(extras.getString(YOU_FIRST)))
                .putExtra(Utils.NAME, extras.getString(NAME))
                .putExtra(Utils.REG_ID, extras.getString(REG_ID))
                .putExtra(Utils.SIDE, extras.getString(SIDE))
                .putExtra(Utils.IS_ORGANIZER, Boolean.valueOf(extras.getString(IS_ORGANIZER)));
        context.startActivity(intent);
    }

    public static class SendResponse extends AsyncTask<String, Void, Void> {

        private boolean mAccepted;
        private boolean isOrganizer;

        public SendResponse(boolean accepted, boolean isOrganizer) {
            mAccepted = accepted;
            this.isOrganizer = isOrganizer;
        }

        @Override
        protected Void doInBackground(String... params) {
            String yourI = params[0];
            String opponentsId = params[1];
            try {
                Utils.sRegService
                        .responseToPlay(yourI, opponentsId, mAccepted, isOrganizer)
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager service = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = service.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    public static void hideKeyboard(Context context) {
        AppCompatActivity act = (AppCompatActivity) context;
        InputMethodManager inputMethodManager = (InputMethodManager) act
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                act.getCurrentFocus().getWindowToken(), 0
        );
    }

}
