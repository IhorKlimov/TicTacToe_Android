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

package com.example.igorklimov.tictactoe.single;

import com.example.igorklimov.tictactoe.Game;
import com.example.igorklimov.tictactoe.Game.Side;
import com.example.igorklimov.tictactoe.MainActivity;

import java.util.Random;

/**
 * Created by Igor Klimov on 2/29/2016.
 */
public class AI {
    private int[] opponentsChoice = new int[2];
    private Random random = new Random();
    private boolean[][] isTaken;
    @Side
    private int playersChar;
    @Side
    private int opponentChar;
    @Side
    private int[][] field;

    public AI(@Side boolean[][] isTaken, @Side int playersChar, @Side int opponentChar,
              @Side int field[][]) {
        this.isTaken = isTaken;
        this.playersChar = playersChar;
        this.opponentChar = opponentChar;
        this.field = field;
    }

    public void makeDecision() {
        if (isCenterEmpty()) return;
        if (opponentIsCloseToWin()) return;
        if (playerIsCloseToWin()) return;
        if (opponentHasOneChar()) return;
        chooseRandom();
    }

    private boolean isCenterEmpty() {
        if (!isTaken[1][1]) {
            opponentsChoice[0] = 1;
            opponentsChoice[1] = 1;
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

    private boolean hasTwoCharsInLine(int r1, int c1, int r2, int c2, int r3, int c3, int side) {
        if (field[r1][c1] == side && field[r2][c2] == side && !isTaken[r3][c3]) {
            opponentsChoice[0] = r3;
            opponentsChoice[1] = c3;
            return true;
        }
        if (field[r1][c1] == side && field[r3][c3] == side && !isTaken[r2][c2]) {
            opponentsChoice[0] = r2;
            opponentsChoice[1] = c2;
            return true;
        }
        if (field[r2][c2] == side && field[r3][c3] == side && !isTaken[r1][c1]) {
            opponentsChoice[0] = r1;
            opponentsChoice[1] = c1;
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

    @SuppressWarnings("WrongConstant")
    private boolean opponentHasOneChar(int r1, int c1, int r2, int c2, int r3, int c3) {
        if (field[r1][c1] == opponentChar && !isTaken[r2][c2] && !isTaken[r3][c3]) {
            opponentsChoice[0] = r3;
            opponentsChoice[1] = c3;
            return true;
        }
        if (field[r2][c2] == opponentChar && !isTaken[r1][c1] && !isTaken[r3][c3]) {
            opponentsChoice[0] = r1;
            opponentsChoice[1] = c1;
            return true;
        }
        if (field[r3][c3] == opponentChar && !isTaken[r1][c1] && !isTaken[r2][c2]) {
            opponentsChoice[0] = r1;
            opponentsChoice[1] = c1;
            return true;
        }
        return false;
    }

    private void chooseRandom() {
        while (true) {
            int r = random.nextInt(3);
            int c = random.nextInt(3);
            if (!isTaken[r][c]) {
                opponentsChoice[0] = r;
                opponentsChoice[1] = c;
                break;
            }
        }
    }

    public int getRow() {
        return opponentsChoice[0];
    }

    public int getCol() {
        return opponentsChoice[1];
    }

}
