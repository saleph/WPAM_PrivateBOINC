package com.privateboinc;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;

import android.os.Process;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public class MemoryUsageCollector {
    private static final int INITIAL_DELAY = 0;
    private static final int SAMPLES_BUFFER_SIZE_DEFAULT = 1000;
    private final int mPeriodInMs;
    private final int mSamplesBufferSize;
    private final Debug.MemoryInfo[] mActivityManagerMemoryInfo;

    private boolean mFirstRead = true;
    private Long mMemTotal;
    private Buffer mMemUsed;
    private Buffer mMemAvailable;
    private Buffer mMemFree;
    private Buffer mCached;
    private Buffer threshold;
    private ActivityManager activityManager;
    private ActivityManager.MemoryInfo activityMemoryInfo;

    private Buffer memoryAM;
    private ScheduledFuture<?> collectorHandle;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private List<MemoryUsageSampleAddedListener> listeners = new ArrayList<>();


    public MemoryUsageCollector(Context context, int periodInMs) {
        this(context, periodInMs, SAMPLES_BUFFER_SIZE_DEFAULT);
    }

    public MemoryUsageCollector(Context context, int periodInMs, int samplesBufferSize) {
        this.mPeriodInMs = periodInMs;
        this.mSamplesBufferSize = samplesBufferSize;
        this.activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        this.activityMemoryInfo = new ActivityManager.MemoryInfo();
        this.mActivityManagerMemoryInfo = activityManager.getProcessMemoryInfo(new int[]{Process.myPid()});
    }

    /**
     * Start collecting samples. Previously collected samples will be discarded.
     */
    public void start() {
        memoryAM = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(mSamplesBufferSize));
        mMemUsed = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(mSamplesBufferSize));
        mMemAvailable = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(mSamplesBufferSize));
        mMemFree = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(mSamplesBufferSize));
        mCached = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(mSamplesBufferSize));
        threshold = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(mSamplesBufferSize));
        final Runnable collector = new Runnable() {
            public void run() {
                Log.i("sample", "sample collection");
                try {
                    read();
                } catch (Exception e) {
                    Log.i("sample", Log.getStackTraceString(e));
                }
            }
        };
        collectorHandle = scheduler.scheduleAtFixedRate(collector, INITIAL_DELAY, mPeriodInMs,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Stop collecting samples.
     */
    public void stop() {
        if (collectorHandle != null && !collectorHandle.isDone()) {
            collectorHandle.cancel(true);
        }
    }

    private void read() {
        try {
            File file = new File("/proc/meminfo");
            String[] memInfoContent = FileUtils.readFileToString(file, Charsets.UTF_8).split(
                    "[\\n]");
            for (String s : memInfoContent) {
                // Memory values. Percentages are calculated in the ActivityMain class.
                if (mFirstRead && s.startsWith("MemTotal:")) {
                    mMemTotal = Long.parseLong(s.split("[ ]+", 3)[1]);
                    mFirstRead = false;
                } else if (s.startsWith("MemFree:"))
                    mMemFree.add(Long.valueOf(s.split("[ ]+", 3)[1]));
                else if (s.startsWith("Cached:"))
                    mCached.add(Long.valueOf(s.split("[ ]+", 3)[1]));
            }
            // http://stackoverflow.com/questions/3170691/how-to-get-current-memory-usage-in-android
            activityManager.getMemoryInfo(activityMemoryInfo);
            if (activityMemoryInfo == null) {
                mMemUsed.add(0L);
                mMemAvailable.add(0L);
                threshold.add(0L);
            } else {
                mMemUsed.add(mMemTotal - activityMemoryInfo.availMem / 1024);
                mMemAvailable.add(activityMemoryInfo.availMem / 1024);
                threshold.add(activityMemoryInfo.threshold / 1024);
            }
            memoryAM.add((long) mActivityManagerMemoryInfo[0].getTotalPrivateDirty());
        } catch (Exception e) {
            e.printStackTrace();
        }
        notifyMemoryUsageSampleAddedListeners();
    }

    private void notifyMemoryUsageSampleAddedListeners() {
        for (MemoryUsageSampleAddedListener listener : listeners) {
            listener.onMemoryUsageSampleAdded();
        }
    }

    /**
     * Add callback for new samples.
     *
     * @param listener callback function to add
     */
    public void registerMemoryUsageSampleAddedListener(MemoryUsageSampleAddedListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Remove callback for new samples.
     *
     * @param listener callback function to remove
     */
    public void unregisterMemoryUsageSampleAddedListener(MemoryUsageSampleAddedListener listener) {
        this.listeners.remove(listener);
    }

    Collection<Long> getMemoryAM() {
        return memoryAM;
    }

    Long getmMemTotal() {
        return mMemTotal;
    }

    Collection<Long> getmMemUsed() {
        return mMemUsed;
    }

    Collection<Long> getmMemAvailable() {
        return mMemAvailable;
    }

    Collection<Long> getmMemFree() {
        return mMemFree;
    }

    Collection<Long> getmCached() {
        return mCached;
    }

    Collection<Long> getThreshold() {
        return threshold;
    }

    int getIntervalRead() {
        return mPeriodInMs;
    }

    int getIntervalUpdate() {
        return mPeriodInMs;
    }

    double getIntervalWidthInSeconds() {
        return mPeriodInMs /1000.0;
    }
}
