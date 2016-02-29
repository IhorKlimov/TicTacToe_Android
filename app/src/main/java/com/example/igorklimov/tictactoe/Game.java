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

package com.example.igorklimov.tictactoe;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.IntDef;
import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.InputMethodManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Igor Klimov on 2/29/2016.
 */
public class Game {
    @Retention(RetentionPolicy.CLASS)
    @IntDef({X, O})
    public @interface Side {
    }

    public static final int X = 500;
    public static final int O = 600;

    public static String toString(@Side int s) {
        if (s == X) {
            return "X";
        } else {
            return "O";
        }
    }

    public static int getColor(@Side int s) {
        if (s == X) {
            return Color.parseColor("#27d38b");
        } else {
            return Color.parseColor("#00c5cd");
        }
    }



}
