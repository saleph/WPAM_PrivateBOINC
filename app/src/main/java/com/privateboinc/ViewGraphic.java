/*
 * 2010-2017 (C) Antonio Redondo
 * http://antonioredondo.com
 * http://github.com/AntonioRedondo/AnotherMonitor
 *
 * Code under the terms of the GNU General Public License v3.
 *
 */

package com.privateboinc;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.Math.floor;

public class ViewGraphic extends TextureView {

    private boolean graphicInitialised;
    private int yTop;
    private int yBottom;
    private int xLeft;
    private int xRight;
    private int graphicHeight;
    private int graphicWidth;
    private int minutes;
    private int seconds;
    private int intervalTotalNumber;
    private Long memTotal;
    private int thickParam;
    private int thickGrid;
    private int thickEdges;
    private int textSizeLegend;
    private Rect bgRect;
    private Paint bgPaint;
    private Paint textPaintLegend;
    private Paint textPaintLegendV;
    private Paint linesEdgePaint;
    private Paint linesGridPaint;
    private Paint cpuAMPaint;
    private Paint memUsedPaint;
    private Paint memAvailablePaint;
    private Paint memFreePaint;
    private Paint cachedPaint;
    private Paint thresholdPaint;
    private Collection<Long> memoryAM;
    private Collection<Long> memUsed, memAvailable, memFree, cached, threshold;
    private ReaderService mSR;
    private Resources res;
    private Thread mThread;
    private Canvas canvas;


    public ViewGraphic(Context context, AttributeSet attrs) {
        super(context, attrs);
        res = getResources();
        float density = res.getDisplayMetrics().density;
        thickGrid = (int) Math.ceil(1 * density);
        thickParam = (int) Math.ceil(1 * density);
        thickEdges = (int) Math.ceil(2 * density);
        textSizeLegend = (int) Math.ceil(10 * density);
    }


    // https://groups.google.com/a/chromium.org/forum/#!topic/graphics-dev/Z0yE-PWQXc4
    // http://www.edu4java.com/en/androidgame/androidgame2.html
    protected void onDrawCustomised(Canvas canvasIn, Thread thread) {
        if (!graphicInitialised) {
            initializeGraphic();
        }
        if (mSR == null || canvasIn == null || thread == null) {
            return;
        }
        mThread = thread;
        canvas = canvasIn;

        // Graphic background
        if (mThread.isInterrupted())
            return;
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (mThread.isInterrupted())
            return;
        canvas.drawRect(bgRect, bgPaint);

        // Horizontal graphic grid lines
        for (float n = 0.1f; n < 1.0f; n = n + 0.2f) {
            drawLine(xLeft, yTop + graphicHeight * n, xRight, yTop + graphicHeight * n,
					linesGridPaint);
        }

        // Vertical graphic grid lines
        int tempVar;
        for (int n = 1; n <= minutes; ++n) {
            tempVar =
                    (int)floor(xRight - n * mSR.getIntervalWidthInSeconds() * floor(60.0 / (mSR.getIntervalRead() / 1000.0)));
            drawLine(tempVar, yTop, tempVar, yBottom, linesGridPaint);
        }
        drawLineLong(memoryAM, cpuAMPaint);
        drawLineLong(memUsed, memUsedPaint);
        drawLineLong(memAvailable, memAvailablePaint);
        drawLineLong(memFree, memFreePaint);
        drawLineLong(cached, cachedPaint);
        drawLineLong(threshold, thresholdPaint);


        // Horizontal edges
        drawLine(xLeft, yTop, xRight, yTop, linesEdgePaint);
        drawLine(xLeft, yBottom, xRight, yBottom, linesEdgePaint);

        // Vertical edges
        drawLine(xLeft, yTop, xLeft, yBottom, linesEdgePaint);
        drawLine(xRight, yBottom, xRight, yTop, linesEdgePaint);

        // Horizontal legend
        int yBottomTextSpace = 25;
        for (int n = 0; n <= minutes; ++n) {
            drawText(n + "'",
                    (int)floor(xLeft + n * mSR.getIntervalWidthInSeconds() * (int) (60 / ((float) mSR.getIntervalRead() / 1000))), yBottom + yBottomTextSpace, textPaintLegend);
        }
        if (minutes == 0) {
            drawText(seconds + "\"", xLeft, yBottom + yBottomTextSpace, textPaintLegend);
        }

        // Vertical legend
        int xLeftTextSpace = 10;
        tempVar = xLeft - xLeftTextSpace;
        drawText("100%", tempVar, yTop + 5, textPaintLegendV);
        if (mThread.isInterrupted())
            return;
        int yLegendSpace = 8;
        drawText("90%", tempVar, yTop + graphicHeight * 0.1f + yLegendSpace,
				textPaintLegendV);
        drawText("70%", tempVar, yTop + graphicHeight * 0.3f + yLegendSpace,
				textPaintLegendV);
        drawText("50%", tempVar, yTop + graphicHeight * 0.5f + yLegendSpace,
				textPaintLegendV);
        drawText("30%", tempVar, yTop + graphicHeight * 0.7f + yLegendSpace,
				textPaintLegendV);
        drawText("10%", tempVar, yTop + graphicHeight * 0.9f + yLegendSpace,
				textPaintLegendV);
        drawText("0%", tempVar, yBottom + yLegendSpace, textPaintLegendV);
    }


    private void drawLineLong(Collection<Long> yColl, Paint paint) {
        List<Long> y = new ArrayList<>(yColl);
        if (y.size() > 1)
            for (int m = 0; m < (y.size() - 1) && m < intervalTotalNumber; ++m) {
                drawLine((int)(xLeft + mSR.getIntervalWidthInSeconds() * m),
                        yBottom - y.get(m) * graphicHeight / memTotal,
                        (int)(xLeft + mSR.getIntervalWidthInSeconds() * m + mSR.getIntervalWidthInSeconds()),
                        yBottom - y.get(m + 1) * graphicHeight / memTotal, paint);
            }
    }


    private void drawLine(Collection<Long> yColl, Paint paint) {
        List<Long> y = new ArrayList<>(yColl);
        if (y.size() > 1)
            for (int m = 0; m < (y.size() - 1) && m < intervalTotalNumber; ++m) {
                drawLine((int)(xLeft + mSR.getIntervalWidthInSeconds() * m),
                        yBottom - y.get(m) * graphicHeight / memTotal,
                        (int)(xLeft + mSR.getIntervalWidthInSeconds() * m + mSR.getIntervalWidthInSeconds()),
                        yBottom - y.get(m + 1) * graphicHeight / memTotal,
                        paint);
            }
    }


    private void initializeGraphic() {
        yTop = (int) (getHeight() * 0.1);
        yBottom = (int) (getHeight() * 0.88);
        xLeft = (int) (getWidth() * 0.14);
        xRight = (int) (getWidth() * 0.94);

        graphicWidth = xRight - xLeft;
        graphicHeight = yBottom - yTop;

        bgRect = new Rect(xLeft, yTop, xRight, yBottom);
        //		graphicPath =  new Path();

        calculateInnerVariables();

        bgPaint = getPaint(Color.LTGRAY, Paint.Align.CENTER, 12, false, 0);
        Paint circlePaint = getPaint(Color.RED, Paint.Align.CENTER, 12, false, 0);

        linesEdgePaint = getPaint(res.getColor(R.color.shadow), Paint.Align.CENTER, 12, false,
				thickEdges);
        linesGridPaint = getPaint(res.getColor(R.color.shadow), Paint.Align.CENTER, 12, false,
				thickGrid);
        linesGridPaint.setStyle(Style.STROKE);
        linesGridPaint.setPathEffect(new DashPathEffect(new float[]{8, 8}, 0));

        memUsedPaint = getPaint(res.getColor(R.color.Orange), Paint.Align.CENTER, 12, false,
				thickParam);
        cpuAMPaint = getPaint(Color.YELLOW, Paint.Align.CENTER, 12, false, thickParam);
        memAvailablePaint = getPaint(Color.MAGENTA, Paint.Align.CENTER, 12, false, thickParam);
        memFreePaint = getPaint(Color.parseColor("#804000"), Paint.Align.CENTER, 12, false,
				thickParam);
        cachedPaint = getPaint(Color.BLUE, Paint.Align.CENTER, 12, false, thickParam);
        thresholdPaint = getPaint(Color.GREEN, Paint.Align.CENTER, 12, false, thickParam);

        textPaintLegend = getPaint(Color.DKGRAY, Paint.Align.CENTER, textSizeLegend, true, 0);
        textPaintLegendV = getPaint(Color.DKGRAY, Paint.Align.RIGHT, textSizeLegend, true, 0);

        graphicInitialised = true;
    }


    private Paint getPaint(int color, Paint.Align textAlign, int textSize, boolean antiAlias, float strokeWidth) {
        Paint p = new Paint();
        p.setColor(color);
        p.setTextSize(textSize);
        p.setTextAlign(textAlign);
        p.setAntiAlias(antiAlias);
        p.setStrokeWidth(strokeWidth);
        return p;
    }


    void setService(ReaderService sr) {
        mSR = sr;
        memoryAM = mSR.getMemoryAM();
        memTotal = mSR.getMemTotal();
        memUsed = mSR.getMemUsed();
        memAvailable = mSR.getMemAvailable();
        memFree = mSR.getMemFree();
        cached = mSR.getCached();
        threshold = mSR.getThreshold();
    }

    void calculateInnerVariables() {
        intervalTotalNumber = (int) Math.ceil(graphicWidth / mSR.getIntervalWidthInSeconds());
        minutes = (int) floor(intervalTotalNumber * mSR.getIntervalRead() / 1000.0 / 60.0);
        seconds = (int) floor(intervalTotalNumber * mSR.getIntervalRead() / 1000.0);
    }

    void drawLine(float startX, float startY, float stopX, float stopY, Paint paint) {
        if (mThread.isInterrupted()) {
            return;
        }
        canvas.drawLine(startX, startY, stopX, stopY, paint);
    }
    
    void drawText(String text, float x, float y, Paint paint) {
        if (mThread.isInterrupted())
            return;
        canvas.drawText(text, x, y, paint);
    }
}
