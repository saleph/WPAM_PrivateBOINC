package com.privateboinc;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class ReaderService extends Service {

    private final CpuUsageCollector cpuUsageCollector = new CpuUsageCollector(1000);
    private final MemoryUsageCollector memoryUsageCollector = new MemoryUsageCollector(this, 1000);

    private BroadcastReceiver receiverClose = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
            sendBroadcast(new Intent(C.actionFinishActivity));
            stopSelf();
        }
    };
    private Notification mNotificationRead;

    @Override
    public void onCreate() {
        cpuUsageCollector.registerCpuUsageSampleAddedListener(new CpuUsageSampleAddedListener() {
            @Override
            public void onCpuUsageSampleAdded() {
                handleCpuSample();
            }
        });
        memoryUsageCollector.registerMemoryUsageSampleAddedListener(new MemoryUsageSampleAddedListener() {
            @Override
            public void onMemoryUsageSampleAdded() {
                handleMemorySample();
            }
        });
        cpuUsageCollector.start();
        memoryUsageCollector.start();

        registerReceiver(receiverClose, new IntentFilter(C.actionClose));


        NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        PendingIntent contentIntent =
                TaskStackBuilder.create(this).addNextIntentWithParentStack(new Intent(this,
                        MainActivity.class)).getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pIClose = PendingIntent.getBroadcast(this, 0, new Intent(C.actionClose),
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                getString(R.string.channelId));
        builder.setContentTitle(getString(R.string.app_name));
        builder.setSmallIcon(R.drawable.icon_bw);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon, null));
        builder.setContentText(getString(R.string.notify_read2));
        builder.setTicker(getString(R.string.notify_read));
        builder.setWhen(0);
        builder.setOngoing(true);
        builder.setContentIntent(contentIntent);
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(getString(R.string.notify_read2)));
        mNotificationRead = builder.build();;

        startForeground(10, mNotificationRead); // If not the AM service will be easily killed
        // when a heavy-use memory app (like a browser or Google Maps) goes onto the foreground
    }

    private void handleMemorySample() {

    }

    private void handleCpuSample() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        class ReaderServiceBinder extends Binder {
            ReaderService getService() {
                return ReaderService.this;
            }
        }
        return new ReaderServiceBinder();
    }
}
