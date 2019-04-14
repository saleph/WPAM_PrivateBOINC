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
    private final long periodInMs;
    private final int samplesBufferSize;
    private final Debug.MemoryInfo[] amMI;

    private boolean firstRead = true;
    private int memTotal;
    private Buffer memUsed, memAvailable, memFree, cached, threshold;
    private ActivityManager activityManager;
    private ActivityManager.MemoryInfo activityMemoryInfo;

    private Buffer memoryAM;
    private ScheduledFuture<?> collectorHandle;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private List<MemoryUsageSampleAddedListener> listeners = new ArrayList<>();


    public MemoryUsageCollector(Context context, long periodInMs) {
        this(context, periodInMs, SAMPLES_BUFFER_SIZE_DEFAULT);
    }

    public MemoryUsageCollector(Context context, long periodInMs, int samplesBufferSize) {
        this.periodInMs = periodInMs;
        this.samplesBufferSize = samplesBufferSize;
        this.activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        this.activityMemoryInfo = new ActivityManager.MemoryInfo();
        this.amMI = activityManager.getProcessMemoryInfo(new int[]{Process.myPid()});
    }

    /**
     * Start collecting samples. Previously collected samples will be discarded.
     */
    public void start() {
        memoryAM = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(samplesBufferSize));
        final Runnable collector = new Runnable() {
            public void run() {
                read();
            }
        };
        collectorHandle = scheduler.scheduleAtFixedRate(collector, INITIAL_DELAY, periodInMs,
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
                if (firstRead && s.startsWith("MemTotal:")) {
                    memTotal = Integer.parseInt(s.split("[ ]+", 3)[1]);
                    firstRead = false;
                } else if (s.startsWith("MemFree:"))
                    memFree.add(s.split("[ ]+", 3)[1]);
                else if (s.startsWith("Cached:"))
                    cached.add(s.split("[ ]+", 3)[1]);
            }
            // http://stackoverflow.com/questions/3170691/how-to-get-current-memory-usage-in-android
            activityManager.getMemoryInfo(activityMemoryInfo);
            if (activityMemoryInfo == null) {
                memUsed.add(String.valueOf(0));
                memAvailable.add(String.valueOf(0));
                threshold.add(String.valueOf(0));
            } else {
                memUsed.add(String.valueOf(memTotal - activityMemoryInfo.availMem / 1024));
                memAvailable.add(String.valueOf(activityMemoryInfo.availMem / 1024));
                threshold.add(String.valueOf(activityMemoryInfo.threshold / 1024));
            }
            memoryAM.add(amMI[0].getTotalPrivateDirty());
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

    Collection<Integer> getMemoryAM() {
        return memoryAM;
    }

    int getMemTotal() {
        return memTotal;
    }

    Collection<String> getMemUsed() {
        return memUsed;
    }

    Collection<String> getMemAvailable() {
        return memAvailable;
    }

    Collection<String> getMemFree() {
        return memFree;
    }

    Collection<String> getCached() {
        return cached;
    }

    Collection<String> getThreshold() {
        return threshold;
    }
}
