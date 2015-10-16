package com.example.igorklimov.tictactoe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Random;

import static com.example.igorklimov.tictactoe.MainActivity.Side.O;
import static com.example.igorklimov.tictactoe.MainActivity.Side.X;

public class MainActivity extends AppCompatActivity {
    private Side[][] field = new Side[3][3];
    private boolean[][] isTaken = new boolean[3][3];
    private final String LOG = "TTT";
    private boolean playersTurn = true;
    private int turnCount = 0;
    private boolean done = false;
    static Side playersChar;
    static Side opponentChar;
    static int playersScore = 0;
    static int opponentsScore = 0;
    static int playerFirst = 2;
    TableRow row1;
    TableRow row2;
    TableRow row3;
    ImageView drawingImageView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    TableLayout grid;
    DisplayMetrics dm;
    float screenDPIy;
    Typeface type;
    ImageButton reset;
    TextView result;
    TextView score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android.support.v7.app.ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
        drawingImageView = (ImageView) findViewById(R.id.DrawingImageView);
        grid = (TableLayout) findViewById(R.id.Grid);
        result = (TextView) findViewById(R.id.result);
        row1 = (TableRow) findViewById(R.id.Row1);
        row2 = (TableRow) findViewById(R.id.Row2);
        row3 = (TableRow) findViewById(R.id.Row3);
        paint = new Paint();
        paint.setColor(Color.parseColor("#f75d11"));
        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenDPIy = dm.ydpi;
        paint.setStrokeWidth(screenDPIy / 20);
        paint.setMaskFilter(new BlurMaskFilter(6, BlurMaskFilter.Blur.NORMAL));
        paint.setStrokeCap(Paint.Cap.ROUND);
        bitmap = Bitmap.createBitmap(getWindowManager()
                .getDefaultDisplay().getWidth(), getWindowManager()
                .getDefaultDisplay().getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        type = Typeface.createFromAsset(getAssets(), "fonts/Rosemary.ttf");
        result.setTypeface(type);
        reset = (ImageButton) findViewById(R.id.reset);
        reset.setVisibility(View.GONE);
        result.setVisibility(View.GONE);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                playerFirst++;
                startActivity(intent);
                finish();
            }
        });
        TextView you = (TextView) findViewById(R.id.you);
        TextView ai = (TextView) findViewById(R.id.AI);
         score = (TextView) findViewById(R.id.score);
        you.append(playersChar.toString());
        ai.append(opponentChar.toString());
        score.append(" " + playersScore + ":" + opponentsScore);
        if (playerFirst % 2 != 0) {
            playersTurn = false;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    opponentsTurn();
                }
            }, 500);
        }
    }


    public void cellClick(View view) {

        if (playersTurn && !done) {
            TextView cell = (TextView) findViewById(view.getId());
            String cellId = cell.getResources().getResourceEntryName(cell.getId()).replace("cell", "");
            int row = Integer.valueOf(cellId.substring(0, 1));
            int col = Integer.valueOf(cellId.substring(1, 2));
            Log.i(LOG, isTaken[row][col] + "");
            if (!isTaken[row][col]) {
                cell.setText(playersChar.toString());
                cell.setTextColor(Color.parseColor(playersChar.getColor()));
                cell.setTypeface(type);
                field[row][col] = playersChar;
                isTaken[row][col] = true;
                Log.i(LOG, "Row: " + row + " Col: " + col);
                playersTurn = false;
                turnCount++;
                checkVictory();
                if (turnCount != 9 && !done) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            opponentsTurn();
                        }
                    }, 500);
                }
            }
        }
    }


    private void opponentsTurn() {
        AI ai = new AI();
        ai.makeDecision();
        int row = ai.getRow();
        int col = ai.getCol();
        String viewID = "cell" + row + "" + col;
        int id = getResources().getIdentifier(viewID, "id", getApplicationContext().getPackageName());
        Log.i(LOG, viewID);
        TextView textView = (TextView) findViewById(id);
        isTaken[row][col] = true;
        if (!done) {
            textView.setText(opponentChar.toString());
            textView.setTextColor(Color.parseColor(opponentChar.getColor()));
            textView.setTypeface(type);
            field[row][col] = opponentChar;
            playersTurn = true;
            turnCount++;
            checkVictory();
        }
    }

    private void checkVictory() {
        if (field[0][0] != null) {
            if (field[0][0] == field[0][1] && field[0][1] == field[0][2]) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawHLine(0);
                        checkWinner(0, 0, false);
                    }
                }, 100);
                return;
            }
        }
        if (field[1][0] != null) {
            if (field[1][0] == field[1][1] && field[1][1] == field[1][2]) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawHLine(1);
                        checkWinner(1, 0, false);
                    }
                }, 100);
                return;
            }
        }
        if (field[2][0] != null) {
            if (field[2][0] == field[2][1] && field[2][1] == field[2][2]) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawHLine(2);
                        checkWinner(2, 0, false);
                    }
                }, 100);
                return;
            }
        }
        if (field[0][0] != null) {
            if (field[0][0] == field[1][0] && field[1][0] == field[2][0]) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawVLine(0);
                        checkWinner(0, 0, false);
                    }
                }, 100);
                return;
            }
        }
        if (field[0][1] != null) {
            if (field[0][1] == field[1][1] && field[1][1] == field[2][1]) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawVLine(1);
                        checkWinner(0, 1, false);
                    }
                }, 100);
                return;
            }
        }
        if (field[0][2] != null) {
            if (field[0][2] == field[1][2] && field[1][2] == field[2][2]) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawVLine(2);
                        checkWinner(0, 2, false);
                    }
                }, 100);
                return;
            }
        }
        if (field[0][0] != null) {
            if (field[0][0] == field[1][1] && field[1][1] == field[2][2]) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawDLine(false);
                        checkWinner(0, 0, false);
                    }
                }, 100);
                return;
            }
        }
        if (field[0][2] != null) {
            if (field[0][2] == field[1][1] && field[1][1] == field[2][0]) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawDLine(true);
                        checkWinner(0, 2, false);
                    }
                }, 100);
                return;
            }
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
        int left = row3.getLeft();
        int right = row3.getRight();
        int top = grid.getTop();
        int bottom = (int) (grid.getBottom() + screenDPIy / 5);
        Log.i(LOG, "top " + top);
        Log.i(LOG, "bottom " + bottom);
        int y = 0;

        if (row == 0) {
            y = (int) ((((bottom - top) / 3) / 2) + (screenDPIy / 5));
        }
        if (row == 1) {
            y = (bottom - top) / 2;
        }
        if (row == 2) {
            int i = bottom - top;
            int t = (i / 3) * 2;
            int m = (i - t) / 2;
            y = (int) (t + m - (screenDPIy / 5));
        }

        drawingImageView.setImageBitmap(bitmap);
        Log.i(LOG, y + " y -------");
        canvas.drawLine(left, y, right, y, paint);
    }

    private void drawVLine(int col) {
        int left = row1.getLeft();
        int right = row1.getRight();
        int top = row1.getTop();
        int bottom = (int) (grid.getBottom() - (screenDPIy / 10));
        Log.i(LOG, "top " + top);
        Log.i(LOG, "bottom " + bottom);
        int x = 0;

        if (col == 0) {
            x = (int) ((((right - left) / 3) / 2) + (screenDPIy / 10));
        }
        if (col == 1) {
            x = (int) (((right - left) / 2) + (screenDPIy / 8));
        }
        if (col == 2) {
            int i = right - left;
            int l = (i / 3) * 2;
            int r = (i - l) / 2;
            x = (int) (l + r + (screenDPIy / 6));
        }
        drawingImageView.setImageBitmap(bitmap);
        canvas.drawLine(x, top, x, bottom, paint);
    }

    private void drawDLine(boolean increasing) {
        int left = row1.getLeft();
        int right = row1.getRight();
        int start;
        int finish;
        if (increasing) {
            start = (int) (grid.getBottom() - (screenDPIy / 5));
            finish = (int) (row1.getTop() + (screenDPIy / 5));
        } else {
            start = (int) (row1.getTop() + (screenDPIy / 5));
            finish = (int) (grid.getBottom() - (screenDPIy / 5));
        }
        drawingImageView.setImageBitmap(bitmap);
        canvas.drawLine(left, start, right, finish, paint);
    }

    private void checkWinner(int row, int col, boolean draw) {
        done = true;
        if (draw) {
            result.setTextColor(Color.GRAY);
            result.setText(R.string.Draw);
        } else if (field[row][col] == playersChar) {
            result.setTextColor(Color.parseColor("#3366FF"));
            result.setText(R.string.YouWin);
            playersScore++;
            score.setText(String.format("Score %d:%d", playersScore,opponentsScore));
        } else {
            result.setTextColor(Color.parseColor("#b22222"));
            result.setText(R.string.YouLose);
            opponentsScore++;
            score.setText(String.format("Score %d:%d", playersScore,opponentsScore));
        }
        new Task().execute();
    }

    private class Task extends AsyncTask<Void, Void, Void> {
        RelativeLayout root = (RelativeLayout) findViewById(R.id.fullscreen_content);
        Bitmap src;
        Bitmap res;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            grid.setDrawingCacheEnabled(true);
            grid.buildDrawingCache();
            src = grid.getDrawingCache();
        }

        @Override
        protected Void doInBackground(Void... params) {
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
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            grid.removeAllViews();
            grid.setBackground(new BitmapDrawable(getResources(), res));
            result.setVisibility(View.VISIBLE);
            reset.setVisibility(View.VISIBLE);
        }
    }

    private class AI {
        private RegionData opponentsChoice = new RegionData();
        private Random random = new Random();

        private void makeDecision() {
            if (isCenterEmpty()) {
                return;
            }

            if (opponentIsCloseToWin()) {
                return;
            }

            if (playerIsCloseToWin()) {
                return;
            }

            if (opponentHasOneChar()) {
                return;
            }
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
            if (hasTwoCharsInLine(0, 0, 0, 1, 0, 2, playersChar)) {
                return true;
            }
            if (hasTwoCharsInLine(1, 0, 1, 1, 1, 2, playersChar)) {
                return true;
            }
            if (hasTwoCharsInLine(2, 0, 2, 1, 2, 2, playersChar)) {
                return true;
            }
            if (hasTwoCharsInLine(0, 0, 1, 0, 2, 0, playersChar)) {
                return true;
            }
            if (hasTwoCharsInLine(0, 1, 1, 1, 2, 1, playersChar)) {
                return true;
            }
            if (hasTwoCharsInLine(0, 2, 1, 2, 2, 2, playersChar)) {
                return true;
            }
            if (hasTwoCharsInLine(0, 0, 1, 1, 2, 2, playersChar)) {
                return true;
            }
            if (hasTwoCharsInLine(0, 2, 1, 1, 2, 0, playersChar)) {
                return true;
            }
            return false;
        }

        private boolean opponentIsCloseToWin() {
            if (hasTwoCharsInLine(0, 0, 0, 1, 0, 2, opponentChar)) {
                return true;
            }
            if (hasTwoCharsInLine(1, 0, 1, 1, 1, 2, opponentChar)) {
                return true;
            }
            if (hasTwoCharsInLine(2, 0, 2, 1, 2, 2, opponentChar)) {
                return true;
            }
            if (hasTwoCharsInLine(0, 0, 1, 0, 2, 0, opponentChar)) {
                return true;
            }
            if (hasTwoCharsInLine(0, 1, 1, 1, 2, 1, opponentChar)) {
                return true;
            }
            if (hasTwoCharsInLine(0, 2, 1, 2, 2, 2, opponentChar)) {
                return true;
            }
            if (hasTwoCharsInLine(0, 0, 1, 1, 2, 2, opponentChar)) {
                return true;
            }
            if (hasTwoCharsInLine(0, 2, 1, 1, 2, 0, opponentChar)) {
                return true;
            }
            return false;
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
            if (opponentHasOneChar(0, 0, 0, 1, 0, 2)) {
                return true;
            }
            if (opponentHasOneChar(1, 0, 1, 1, 1, 2)) {
                return true;
            }
            if (opponentHasOneChar(2, 0, 2, 1, 2, 2)) {
                return true;
            }
            if (opponentHasOneChar(0, 0, 1, 0, 2, 0)) {
                return true;
            }
            if (opponentHasOneChar(0, 1, 1, 1, 2, 1)) {
                return true;
            }
            if (opponentHasOneChar(0, 2, 1, 2, 2, 2)) {
                return true;
            }
            if (opponentHasOneChar(0, 0, 1, 1, 2, 2)) {
                return true;
            }
            if (opponentHasOneChar(0, 2, 1, 1, 2, 0)) {
                return true;
            }
            return false;
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

        public RegionData(int row, int col) {
            this.row = row;
            this.col = col;
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
}
