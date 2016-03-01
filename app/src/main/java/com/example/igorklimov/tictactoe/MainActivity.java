package com.example.igorklimov.tictactoe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.igorklimov.tictactoe.Game.Side;
import com.example.igorklimov.tictactoe.bluetooth.BluetoothService;
import com.example.igorklimov.tictactoe.databinding.ActivityGameBinding;
import com.example.igorklimov.tictactoe.res.Constants;
import com.example.igorklimov.tictactoe.single.AI;
import com.example.igorklimov.tictactoe.wifi.Utils;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.iklimov.tictactoe.backend.game.model.Player;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
import static com.example.igorklimov.tictactoe.Game.*;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "TTT";


    public static BluetoothService service;
    @Side
    private int[][] field = new int[3][3];
    private boolean[][] isTaken = new boolean[3][3];
    public static String playersName;
    public static String opponentsName;
    static boolean playersTurn;
    public static boolean btGame;
    private int turnCount = 0;
    private boolean done = false;
    @Side
    public static int playersChar;
    @Side
    public static int opponentChar;
    static int playersScore = 0;
    static int opponentsScore = 0;
    public static int playerFirst = 2;
    private static Typeface sMakeOut;
    private static Typeface sRosemary;
    private ActivityGameBinding mBinding;
    private AI mAi;
    private String opponentsRegId;
    private BroadcastReceiver mReceiver;
    private Context mContext;
    private boolean wifiGame;
    private boolean isOrganizer;
    private boolean isNewGame;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil
                .setContentView(this, R.layout.activity_game);

        mContext = this;

        if (sMakeOut == null) {
            sMakeOut = Typeface.createFromAsset(getAssets(), "fonts/MakeOut.ttf");
            sRosemary = Typeface.createFromAsset(getAssets(), "fonts/Rosemary.ttf");
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            wifiGame = extras.getBoolean(Utils.IS_WIFI_GAME);
            if (wifiGame) {
                playersTurn = extras.getBoolean(Utils.YOU_FIRST);
                opponentsName = extras.getString(Utils.NAME);
                opponentsRegId = extras.getString(Utils.REG_ID);
                playersName = getString(R.string.you);
                isOrganizer = extras.getBoolean(Utils.IS_ORGANIZER);
                if (extras.getString(Utils.SIDE).equals("X")) {
                    playersChar = X;
                    opponentChar = O;
                } else {
                    playersChar = O;
                    opponentChar = X;
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

                                if (action.equals(Utils.TURN)) {
                                    int row = Integer.parseInt(extras.getString(Utils.ROW));
                                    int col = Integer.parseInt(extras.getString(Utils.COL));
                                    setText(row, col);
                                } else if (action.equals(Utils.START_GAME)) {
                                    isNewGame = true;
                                    Utils.startGame(mContext, extras);
                                    finish();
                                } else if (action.equals(Utils.IS_OFFLINE)) {
                                    String message = context.getString(R.string.opponent_offline, opponentsName);
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                };
            }
        }


        mBinding.result.setTypeface(sRosemary);
        mBinding.you.append(playersName + ": " + Game.toString(playersChar));
        mBinding.opponent.append(opponentsName + ": " + Game.toString(opponentChar));
        mBinding.score.append(playersScore + ":" + opponentsScore);

        if (!btGame && !wifiGame) {
            mAi = new AI(isTaken, playersChar, opponentChar, field);
            if (playerFirst % 2 != 0) {
                playersTurn = false;
                if (!btGame) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AiTurn();
                        }
                    }, 500);
                }
            } else {
                playersTurn = true;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wifiGame) {
            registerReceiver(mReceiver, new IntentFilter("com.google.android.c2dm.intent.RECEIVE"));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (wifiGame) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (wifiGame && !isNewGame) {
            new SendOffline().execute();
        }
    }

    private void startSingleGame() {
        Intent intent = new Intent(this, MainActivity.class);
        playerFirst++;
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(this, playersTurn
                ? getString(R.string.your_turn)
                : getString(R.string.opponents_turn), LENGTH_SHORT)
                .show();
        if (btGame) {
            service.setHandler(handler);

        }
    }

    public void cellClick(View view) {
        if (playersTurn && !done) {
            TextView v = (TextView) view;
            String cellId = view.getResources()
                    .getResourceEntryName(view.getId()).replace("cell", "");
            int row = Integer.valueOf(cellId.substring(0, 1));
            int col = Integer.valueOf(cellId.substring(1, 2));
            if (!isTaken[row][col]) {
                v.setText(Game.toString(playersChar));
                v.setTextColor(Game.getColor(playersChar));
                v.setTypeface(sMakeOut);
                field[row][col] = playersChar;
                isTaken[row][col] = true;
                if (btGame) service.write((row + " " + col).getBytes());
                playersTurn = false;
                turnCount++;
                checkVictory();
                if (!btGame && turnCount != 9 && !done && !wifiGame) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AiTurn();
                        }
                    }, 500);
                } else if (wifiGame) {
                    new SendTurn().execute(row + "", col + "");
                }
            }
        }
    }

    private void AiTurn() {
        mAi.makeDecision();
        int row = mAi.getRow();
        int col = mAi.getCol();
        setText(row, col);
    }

    private void setText(int row, int col) {
        Log.d(LOG_TAG, "setText: " + row + " " + col);
        TextView view = null;
        switch (row) {
            case 0:
                if (col == 0) {
                    view = mBinding.cell00;
                } else if (col == 1) {
                    view = mBinding.cell01;
                } else if (col == 2) {
                    view = mBinding.cell02;
                }
                break;
            case 1:
                if (col == 0) {
                    view = mBinding.cell10;
                } else if (col == 1) {
                    view = mBinding.cell11;
                } else if (col == 2) {
                    view = mBinding.cell12;
                }
                break;
            case 2:
                if (col == 0) {
                    view = mBinding.cell20;
                } else if (col == 1) {
                    view = mBinding.cell21;
                } else if (col == 2) {
                    view = mBinding.cell22;
                }
                break;
        }

        isTaken[row][col] = true;
        if (!done) {
            view.setText(Game.toString(opponentChar));
            view.setTextColor(Game.getColor(opponentChar));
            view.setTypeface(sMakeOut);
            field[row][col] = opponentChar;
            playersTurn = true;
            turnCount++;
            checkVictory();
        }
    }

    private void checkVictory() {
        if (field[0][0] == field[0][1] && field[0][1] == field[0][2] && field[0][0] != 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawHLine(0);
                    checkWinner(0, 0, false);
                }
            }, 100);
            return;
        }
        if (field[1][0] == field[1][1] && field[1][1] == field[1][2] && field[1][0] != 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawHLine(1);
                    checkWinner(1, 0, false);
                }
            }, 100);
            return;
        }
        if (field[2][0] == field[2][1] && field[2][1] == field[2][2] && field[2][0] != 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawHLine(2);
                    checkWinner(2, 0, false);
                }
            }, 100);
            return;
        }
        if (field[0][0] == field[1][0] && field[1][0] == field[2][0] && field[0][0] != 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawVLine(0);
                    checkWinner(0, 0, false);
                }
            }, 100);
            return;
        }
        if (field[0][1] == field[1][1] && field[1][1] == field[2][1] && field[0][1] != 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawVLine(1);
                    checkWinner(0, 1, false);
                }
            }, 100);
            return;
        }
        if (field[0][2] == field[1][2] && field[1][2] == field[2][2] && field[0][2] != 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawVLine(2);
                    checkWinner(0, 2, false);
                }
            }, 100);
            return;
        }
        if (field[0][0] == field[1][1] && field[1][1] == field[2][2] && field[0][0] != 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawDLine(false);
                    checkWinner(0, 0, false);
                }
            }, 100);
            return;
        }
        if (field[0][2] == field[1][1] && field[1][1] == field[2][0] && field[0][2] != 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawDLine(true);
                    checkWinner(0, 2, false);
                }
            }, 100);
            return;
        }
        if (turnCount == 9 && !done) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkWinner(0, 0, true);
                }
            }, 100);
        }
    }

    private void drawHLine(int row) {
        int left = mBinding.grid.getLeft();
        int right = mBinding.grid.getRight();
        Rect r = new Rect();

        if (row == 0) {
            mBinding.cell00.getGlobalVisibleRect(r);
        } else if (row == 1) {
            mBinding.cell10.getGlobalVisibleRect(r);
        } else if (row == 2) {
            mBinding.cell20.getGlobalVisibleRect(r);
        }
        int y = ((r.bottom - r.top) / 2) + r.top;

        drawLine(left, y, right, y);
    }

    private void drawVLine(int col) {
        int top = mBinding.grid.getTop();
        int bottom = mBinding.grid.getBottom();
        Rect r = new Rect();

        if (col == 0) {
            mBinding.cell00.getGlobalVisibleRect(r);
        } else if (col == 1) {
            mBinding.cell01.getGlobalVisibleRect(r);
        } else if (col == 2) {
            mBinding.cell02.getGlobalVisibleRect(r);
        }

        int x = ((r.right - r.left) / 2) + r.left;

        drawLine(x, top, x, bottom);
    }

    private void drawLine(int startX, int startY, int endX, int endY) {
        mBinding.lineView.setRect(new Rect(startX, startY, endX, endY));
        mBinding.lineView.setVisibility(VISIBLE);
        mBinding.lineView.invalidate();
    }

    private void drawDLine(boolean rising) {
        int left = mBinding.grid.getLeft();
        int right = mBinding.grid.getRight();
        int start;
        int finish;

        if (rising) {
            start = mBinding.grid.getBottom();
            finish = mBinding.grid.getTop();
        } else {
            start = mBinding.grid.getTop();
            finish = mBinding.grid.getBottom();
        }

        drawLine(left, start, right, finish);
    }

    @SuppressWarnings("WrongConstant")
    private void checkWinner(int row, int col, boolean draw) {
        done = true;
        if (draw) {
            mBinding.result.setTextColor(Color.GRAY);
            mBinding.result.setText(R.string.Draw);
        } else if (field[row][col] == playersChar) {
            mBinding.result.setTextColor(Color.parseColor("#4CAF50"));
            mBinding.result.setText(R.string.YouWin);
            playersScore++;
            mBinding.score.setText(String.format("Score %d:%d", playersScore, opponentsScore));
        } else {
            mBinding.result.setTextColor(Color.parseColor("#b22222"));
            mBinding.result.setText(R.string.YouLose);
            opponentsScore++;
            mBinding.score.setText(String.format("Score %d:%d", playersScore, opponentsScore));
        }
        mBinding.result.setVisibility(VISIBLE);
        if (!wifiGame || isOrganizer) {
            mBinding.reset.setVisibility(VISIBLE);
        }
    }

    public void resetGame(View view) {
        if (btGame) {
            service.write("Reset".getBytes());
        } else if (wifiGame) {
            mBinding.reset.setClickable(false);
            new Utils.SendResponse(true, true).execute(Utils.sUserId, opponentsRegId);
        } else {
            startSingleGame();
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if (readMessage.equals("Reset")) {
                        startSingleGame();
                    } else {
                        int row = Integer.parseInt(readMessage.substring(0, 1));
                        int col = Integer.parseInt(readMessage.substring(2, 3));
                        setText(row, col);
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(Constants.TOAST), LENGTH_SHORT)
                            .show();
                    break;
            }
            return false;
        }
    });


    private class SendTurn extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                Utils.sRegService.sendTurn(opponentsRegId, params[0], params[1]).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class SendOffline extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Utils.sRegService.sendOffline(opponentsRegId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
