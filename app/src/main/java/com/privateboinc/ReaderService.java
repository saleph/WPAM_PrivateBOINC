package com.privateboinc;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import java.util.Collection;

public class ReaderService extends Service {

    private MemoryUsageCollector memoryUsageCollector;

    private BroadcastReceiver receiverClose = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
            sendBroadcast(new Intent(C.actionFinishActivity));
            stopSelf();
        }
    };

    public class ReaderServiceBinder extends Binder {
        ReaderService getService() {
            return ReaderService.this;
        }
    }

    @Override
    public void onCreate() {
        log("created:");
        this.memoryUsageCollector = new MemoryUsageCollector(this, 1000, 10);
        memoryUsageCollector.registerMemoryUsageSampleAddedListener(new MemoryUsageSampleAddedListener() {
            @Override
            public void onMemoryUsageSampleAdded() {
                handleMemorySample();
            }
        });
        memoryUsageCollector.start();

        //registerReceiver(receiverClose, new IntentFilter(C.actionClose));
        startMyOwnForeground();
    }


    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.icon_bw)
                .setContentTitle("App is collecting device usage statistics")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    private void handleMemorySample() {
        log("memory:" + memoryUsageCollector.getMemUsed().toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log("on destroy");
        memoryUsageCollector.stop();
        stopForeground(true);
    }

    private void log(String msg) {
        Log.i("sample", msg);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ReaderServiceBinder();
    }

    Collection<Integer> getMemoryAM() {
        return memoryUsageCollector.getMemoryAM();
    }

    int getMemTotal() {
        return memoryUsageCollector.getMemTotal() ;
    }

    Collection<String> getMemUsed() {
        return memoryUsageCollector.getMemUsed();
    }

    Collection<String> getMemAvailable() {
        return memoryUsageCollector.getMemAvailable();
    }

    Collection<String> getMemFree() {
        return memoryUsageCollector.getMemFree();
    }

    Collection<String> getCached() {
        return memoryUsageCollector.getCached();
    }

    Collection<String> getThreshold() {
        return memoryUsageCollector.getThreshold();
    }

    int getIntervalRead() {
        return memoryUsageCollector.getIntervalRead();
    }

    int getIntervalUpdate() {
        return memoryUsageCollector. getIntervalUpdate();
    }

    int getIntervalWidth() {
        return memoryUsageCollector.getIntervalWidth();
    }
}
