package com.example.igorklimov.tictactoe;

import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.igorklimov.tictactoe.databinding.ActivityGameBinding;
import com.example.igorklimov.tictactoe.res.Constants;

import java.util.Random;

import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "TTT";
    static BluetoothService service;
    private Side[][] field = new Side[3][3];
    private boolean[][] isTaken = new boolean[3][3];
    static String playersName;
    static String opponentsName;
    static boolean playersTurn;
    static boolean btGame;
    private int turnCount = 0;
    private boolean done = false;
    static Side playersChar;
    static Side opponentChar;
    static int playersScore = 0;
    static int opponentsScore = 0;
    static int playerFirst = 2;
    private static Typeface sMakeOut;
    private static Typeface sRosemary;
    private ActivityGameBinding mBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil
                .setContentView(this, R.layout.activity_game);

        if (sMakeOut == null) {
            sMakeOut = Typeface.createFromAsset(getAssets(), "fonts/MakeOut.ttf");
            sRosemary = Typeface.createFromAsset(getAssets(), "fonts/Rosemary.ttf");
        }

        mBinding.result.setTypeface(sRosemary);
        mBinding.you.append(playersName + ": " + playersChar.toString());
        mBinding.opponent.append(opponentsName + ": " + opponentChar.toString());
        mBinding.score.append(" " + playersScore + ":" + opponentsScore);
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

    private void startNewGame() {
        Intent intent = new Intent(this, MainActivity.class);
        playerFirst++;
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (btGame) {
            service.setHandler(handler);
            Toast.makeText(
                    this, playersTurn ? getString(R.string.your_turn) : getString(R.string.opponents_turn), LENGTH_SHORT)
                    .show();
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
                v.setText(playersChar.toString());
                v.setTextColor(Color.parseColor(playersChar.getColor()));
                v.setTypeface(sMakeOut);
                field[row][col] = playersChar;
                isTaken[row][col] = true;
                if (btGame) service.write((row + " " + col).getBytes());
                playersTurn = false;
                turnCount++;
                checkVictory();
                if (!btGame && turnCount != 9 && !done) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AiTurn();
                        }
                    }, 500);
                }
            }
        }
    }

    private void AiTurn() {
        AI ai = new AI();
        ai.makeDecision();
        int row = ai.getRow();
        int col = ai.getCol();
        setText(row, col);
    }

    private void setText(int row, int col) {
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
            view.setText(opponentChar.toString());
            view.setTextColor(Color.parseColor(opponentChar.getColor()));
            view.setTypeface(sMakeOut);
            field[row][col] = opponentChar;
            playersTurn = true;
            turnCount++;
            checkVictory();
        }
    }

    private void checkVictory() {
        if (field[0][0] != null && field[0][0] == field[0][1] && field[0][1] == field[0][2]) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawHLine(0);
                    checkWinner(0, 0, false);
                }
            }, 100);
            return;
        }
        if (field[1][0] != null && field[1][0] == field[1][1] && field[1][1] == field[1][2]) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawHLine(1);
                    checkWinner(1, 0, false);
                }
            }, 100);
            return;
        }
        if (field[2][0] != null && field[2][0] == field[2][1] && field[2][1] == field[2][2]) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawHLine(2);
                    checkWinner(2, 0, false);
                }
            }, 100);
            return;
        }
        if (field[0][0] != null && field[0][0] == field[1][0] && field[1][0] == field[2][0]) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawVLine(0);
                    checkWinner(0, 0, false);
                }
            }, 100);
            return;
        }
        if (field[0][1] != null && field[0][1] == field[1][1] && field[1][1] == field[2][1]) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawVLine(1);
                    checkWinner(0, 1, false);
                }
            }, 100);
            return;
        }
        if (field[0][2] != null && field[0][2] == field[1][2] && field[1][2] == field[2][2]) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawVLine(2);
                    checkWinner(0, 2, false);
                }
            }, 100);
            return;
        }
        if (field[0][0] != null && field[0][0] == field[1][1] && field[1][1] == field[2][2]) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawDLine(false);
                    checkWinner(0, 0, false);
                }
            }, 100);
            return;
        }
        if (field[0][2] != null && field[0][2] == field[1][1] && field[1][1] == field[2][0]) {
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
//        new Task().execute();
        mBinding.result.setVisibility(VISIBLE);
        mBinding.reset.setVisibility(VISIBLE);
    }

    public void resetGame(View view) {
        if (btGame) service.write("Reset".getBytes());
        startNewGame();
    }

    private class Task extends AsyncTask<Void, Void, Void> {
        Bitmap src;
        Bitmap res;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mBinding.grid.setDrawingCacheEnabled(true);
            mBinding.grid.buildDrawingCache();
            src = mBinding.grid.getDrawingCache();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                res = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
                RenderScript renderScript = RenderScript.create(getApplicationContext());
                Allocation blurInput = Allocation.createFromBitmap(renderScript, src);
                Allocation blurOutput = Allocation.createFromBitmap(renderScript, res);
                ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
                blur.setInput(blurInput);
                blur.setRadius(18);
                blur.forEach(blurOutput);
                blurOutput.copyTo(res);
                renderScript.destroy();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mBinding.grid.removeAllViews();
                mBinding.grid.setBackground(new BitmapDrawable(getResources(), res));
            }
            mBinding.result.setVisibility(VISIBLE);
            mBinding.reset.setVisibility(VISIBLE);
        }
    }

    private class AI {
        private RegionData opponentsChoice = new RegionData();
        private Random random = new Random();

        private void makeDecision() {
            if (isCenterEmpty()) return;
            if (opponentIsCloseToWin()) return;
            if (playerIsCloseToWin()) return;
            if (opponentHasOneChar()) return;
            chooseRandom();
        }

        private boolean isCenterEmpty() {
            if (!isTaken[1][1]) {
                opponentsChoice.setRow(1);
                opponentsChoice.setCol(1);
                return true;
            }
            return false;
        }

        private boolean playerIsCloseToWin() {
            return hasTwoCharsInLine(0, 0, 0, 1, 0, 2, playersChar)
                    || hasTwoCharsInLine(1, 0, 1, 1, 1, 2, playersChar)
                    || hasTwoCharsInLine(2, 0, 2, 1, 2, 2, playersChar)
                    || hasTwoCharsInLine(0, 0, 1, 0, 2, 0, playersChar)
                    || hasTwoCharsInLine(0, 1, 1, 1, 2, 1, playersChar)
                    || hasTwoCharsInLine(0, 2, 1, 2, 2, 2, playersChar)
                    || hasTwoCharsInLine(0, 0, 1, 1, 2, 2, playersChar)
                    || hasTwoCharsInLine(0, 2, 1, 1, 2, 0, playersChar);
        }

        private boolean opponentIsCloseToWin() {
            return hasTwoCharsInLine(0, 0, 0, 1, 0, 2, opponentChar)
                    || hasTwoCharsInLine(1, 0, 1, 1, 1, 2, opponentChar)
                    || hasTwoCharsInLine(2, 0, 2, 1, 2, 2, opponentChar)
                    || hasTwoCharsInLine(0, 0, 1, 0, 2, 0, opponentChar)
                    || hasTwoCharsInLine(0, 1, 1, 1, 2, 1, opponentChar)
                    || hasTwoCharsInLine(0, 2, 1, 2, 2, 2, opponentChar)
                    || hasTwoCharsInLine(0, 0, 1, 1, 2, 2, opponentChar)
                    || hasTwoCharsInLine(0, 2, 1, 1, 2, 0, opponentChar);
        }

        private boolean hasTwoCharsInLine(int r1, int c1, int r2, int c2, int r3, int c3, Side side) {
            if (field[r1][c1] == side && field[r2][c2] == side && !isTaken[r3][c3]) {
                opponentsChoice.row = r3;
                opponentsChoice.col = c3;
                return true;
            }
            if (field[r1][c1] == side && field[r3][c3] == side && !isTaken[r2][c2]) {
                opponentsChoice.row = r2;
                opponentsChoice.col = c2;
                return true;
            }
            if (field[r2][c2] == side && field[r3][c3] == side && !isTaken[r1][c1]) {
                opponentsChoice.row = r1;
                opponentsChoice.col = c1;
                return true;
            }
            return false;
        }

        private boolean opponentHasOneChar() {
            return opponentHasOneChar(0, 0, 0, 1, 0, 2)
                    || opponentHasOneChar(1, 0, 1, 1, 1, 2)
                    || opponentHasOneChar(2, 0, 2, 1, 2, 2)
                    || opponentHasOneChar(0, 0, 1, 0, 2, 0)
                    || opponentHasOneChar(0, 1, 1, 1, 2, 1)
                    || opponentHasOneChar(0, 2, 1, 2, 2, 2)
                    || opponentHasOneChar(0, 0, 1, 1, 2, 2)
                    || opponentHasOneChar(0, 2, 1, 1, 2, 0);
        }

        private boolean opponentHasOneChar(int r1, int c1, int r2, int c2, int r3, int c3) {
            if (field[r1][c1] == opponentChar && !isTaken[r2][c2] && !isTaken[r3][c3]) {
                opponentsChoice.row = r3;
                opponentsChoice.col = c3;
                return true;
            }
            if (field[r2][c2] == opponentChar && !isTaken[r1][c1] && !isTaken[r3][c3]) {
                opponentsChoice.row = r1;
                opponentsChoice.col = c1;
                return true;
            }
            if (field[r3][c3] == opponentChar && !isTaken[r1][c1] && !isTaken[r2][c2]) {
                opponentsChoice.row = r1;
                opponentsChoice.col = c1;
                return true;
            }
            return false;
        }

        private void chooseRandom() {
            while (true) {
                int r = random.nextInt(3);
                int c = random.nextInt(3);
                if (!isTaken[r][c]) {
                    opponentsChoice.setRow(r);
                    opponentsChoice.setCol(c);
                    break;
                }
            }
        }

        public int getRow() {
            return opponentsChoice.getRow();
        }

        public int getCol() {
            return opponentsChoice.getCol();
        }

    }

    private static class RegionData {
        private int row;
        private int col;

        public RegionData() {
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public void setCol(int col) {
            this.col = col;
        }
    }

    enum Side {
        O {
            @Override
            public String getColor() {
                return "#00c5cd";
            }

            @Override
            public String toString() {
                return "O";
            }
        }, X {
            @Override
            public String getColor() {
                return "#27d38b";
            }

            @Override
            public String toString() {
                return "X";
            }
        };

        public abstract String getColor();

        public abstract String toString();
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
                        startNewGame();
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
}
