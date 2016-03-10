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

package com.example.igorklimov.tictactoe.res;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;

/**
 * Created by Igor Klimov on 2/25/2016.
 */
public class LineView extends View {
    private static final String LOG_TAG = "MyView";

    private Paint mPaint;
    private Rect mRect;

    public LineView(Context context) {
        super(context);
        setPaint(context);
    }

    public LineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPaint(context);
    }

    public LineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setPaint(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(LOG_TAG, "onDraw: " + mRect.top);
        Log.d(LOG_TAG, "onDraw: " + mRect.left);
        canvas.drawLine(mRect.left, mRect.top, mRect.right, mRect.bottom, mPaint);
    }

    public void setRect(Rect rect) {
        mRect = rect;
    }

    private void setPaint(Context context) {
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor("#f75d11"));
        int width = (int) (context.getResources().getDisplayMetrics().density * 12);
        mPaint.setStrokeWidth(width);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }
}
