package com.privateboinc;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewManager;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class ActivityMain extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private boolean canvasLocked;
    private boolean orientationChanged;
    private int intervalUpdate;
    private int navigationBarHeight;
    private int animDuration = 200;
    private float sD;
    private SharedPreferences mPrefs;

    private FrameLayout mLGraphicSurface;

    private LinearLayout mLFeedback;
    private LinearLayout mLWelcome;

    private TextView mTVMemTotal;
    private TextView mTVMemUsed;
    private TextView mTVMemAvailable;
    private TextView mTVMemFree;
    private TextView mTVCached;
    private TextView mTVThreshold;
    private TextView mTVMemUsedP;
    private TextView mTVMemAvailableP;
    private TextView mTVMemFreeP;
    private TextView mTVCachedP;
    private TextView mTVThresholdP;

    private DecimalFormat mFormat = new DecimalFormat("##,###,##0"),
            mFormatPercent = new DecimalFormat("##0.0");
    private Resources res;

    private ViewGraphic mVG;

    private PopupWindow mPWMenu;

    private ReaderService mSR;

    private Intent tempIntent;
    private Handler mHandler = new Handler();
    private Handler mHandlerVG = new Handler();
    private Thread mThread;
    private Runnable drawRunnableGraphic = new Runnable() {
        @Override
        public void run() {
            mThread = new Thread() {
                @Override
                public void run() {
                    Canvas canvas;
                    if (canvasLocked) {
                        return;
                    }
                    canvas = mVG.lockCanvas();
                    if (canvas == null) {
                        return;
                    }
                    canvasLocked = true;
                    mVG.onDrawCustomised(canvas, mThread);
                    try {
                        mVG.unlockCanvasAndPost(canvas);
                    } catch (IllegalStateException e) {
                        Log.w("Activity main: ", e.getMessage());
                    }

                    canvasLocked = false;
                }
            };
            mThread.start();
        }
    };
    private Runnable drawRunnable = new Runnable() {
        @SuppressLint("NewApi")
        @Override
        public void run() {
            mHandler.postDelayed(this, C.defaultIntervalUpdate);
            Log.i("main", "post delayed");
            if (mSR != null) {
                mHandlerVG.post(drawRunnableGraphic);

                setTextLabelMemory(mTVMemUsed, mTVMemUsedP, mSR.getMemUsed());
                setTextLabelMemory(mTVMemAvailable, mTVMemAvailableP, mSR.getMemAvailable());
                setTextLabelMemory(mTVMemFree, mTVMemFreeP, mSR.getMemFree());
                setTextLabelMemory(mTVCached, mTVCachedP, mSR.getCached());
                setTextLabelMemory(mTVThreshold, mTVThresholdP, mSR.getThreshold());

            }
        }
    };
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mSR = ((ReaderService.ReaderServiceBinder) service).getService();

            mVG.setService(mSR);

            mTVMemTotal.setText(mFormat.format(mSR.getMemTotal()) + C.kB);

            mHandler.removeCallbacks(drawRunnable);
            mHandler.post(drawRunnable);

            if (tempIntent != null) {
                tempIntent.putExtra(C.screenRotated, true);
                onActivityResult(1, 1, tempIntent);
                tempIntent = null;
            } else onActivityResult(1, 1, null);

        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mSR = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent serviceIntent = new Intent(getApplicationContext(), ReaderService.class);
        startService(serviceIntent);
        mVG = findViewById(R.id.ANGraphic);
        setContentView(R.layout.activity_main);
        {
            super.onCreate(savedInstanceState);
            startService(new Intent(this, ReaderService.class));
            setContentView(R.layout.activity_main);

            mPrefs = getSharedPreferences(getString(R.string.app_name) + C.prefs, MODE_PRIVATE);
            intervalUpdate = C.defaultIntervalUpdate;

            res = getResources();
            sD = res.getDisplayMetrics().density;

            sD = res.getDisplayMetrics().density;
            int orientation = res.getConfiguration().orientation;
            int statusBarHeight = res.getDimensionPixelSize(res.getIdentifier(C.sbh, C.dimen, C.android));


            if (savedInstanceState != null && !savedInstanceState.isEmpty() && savedInstanceState.getInt(C.orientation) != orientation)
                orientationChanged = true;

            mVG = findViewById(R.id.ANGraphic);

            mLGraphicSurface = findViewById(R.id.LGraphicButton);

            if (Build.VERSION.SDK_INT >= 19) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                float sSW = res.getConfiguration().smallestScreenWidthDp;

                if (!ViewConfiguration.get(this).hasPermanentMenuKey() && !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME)
                        && (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || sSW > 560)) {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                    navigationBarHeight = res.getDimensionPixelSize(res.getIdentifier(C.nbh, C.dimen, C.android));
                    if (navigationBarHeight == 0)
                        navigationBarHeight = (int) (48 * sD);

                    FrameLayout nb = findViewById(R.id.LNavigationBar);
                    nb.setVisibility(View.VISIBLE);
                    ((FrameLayout.LayoutParams) nb.getLayoutParams()).height = navigationBarHeight;
                    ((FrameLayout.LayoutParams) mVG.getLayoutParams()).setMargins(0, 0, 0, navigationBarHeight);
                    ((FrameLayout.LayoutParams) mLGraphicSurface.getLayoutParams()).setMargins(0, 0, 0, navigationBarHeight);

                }

            }

            LinearLayout mLMemUsed = findViewById(R.id.LMemUsed);
            mLMemUsed.setTag(C.memUsed);

            LinearLayout mLMemAvailable = findViewById(R.id.LMemAvailable);
            mLMemAvailable.setTag(C.memAvailable);

            LinearLayout mLMemFree = findViewById(R.id.LMemFree);
            mLMemFree.setTag(C.memFree);

            LinearLayout mLCached = findViewById(R.id.LCached);
            mLCached.setTag(C.cached);

            LinearLayout mLThreshold = findViewById(R.id.LThreshold);
            mLThreshold.setTag(C.threshold);

            mTVMemTotal = findViewById(R.id.TVMemTotal);
            mTVMemUsed = findViewById(R.id.TVMemUsed);
            mTVMemUsedP = findViewById(R.id.TVMemUsedP);
            mTVMemAvailable = findViewById(R.id.TVMemAvailable);
            mTVMemAvailableP = findViewById(R.id.TVMemAvailableP);
            mTVMemFree = findViewById(R.id.TVMemFree);
            mTVMemFreeP = findViewById(R.id.TVMemFreeP);
            mTVCached = findViewById(R.id.TVCached);
            mTVCachedP = findViewById(R.id.TVCachedP);
            mTVThreshold = findViewById(R.id.TVThreshold);
            mTVThresholdP = findViewById(R.id.TVThresholdP);

            mVG.setOpaque(false);

            mVG.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                }
            });
            mVG.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mVG.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mLGraphicSurface.getLayoutParams();

                    lp.setMargins((int) (mVG.getWidth() * 0.14), (int) (mVG.getHeight() * 0.1), (int) (mVG.getWidth() * 0.06), (int) (mVG.getHeight() * 0.12) + navigationBarHeight);
                }
            });

            if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, C.storagePermission);

            if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
                canvasLocked = savedInstanceState.getBoolean(C.canvasLocked);
            }

            if (mPrefs.getBoolean(C.welcome, true)) {
                mPrefs.edit().putLong(C.welcomeDate, Calendar.getInstance(TimeZone.getTimeZone(C.europeLondon)).getTimeInMillis()).apply();
                ViewStub v = findViewById(R.id.VSWelcome);
                if (v != null) {
                    mLWelcome = (LinearLayout) v.inflate();

                    int bottomMargin = 0;
                    if (Build.VERSION.SDK_INT >= 19)
                        bottomMargin = navigationBarHeight;
                    ((FrameLayout.LayoutParams) mLWelcome.getLayoutParams()).setMargins(0, 0, 0, (int) (35 * sD) + bottomMargin);

                    (mLWelcome.findViewById(R.id.BHint)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mPrefs.edit().putBoolean(C.welcome, false).apply();
                            mLWelcome.animate().setDuration(animDuration).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    ((ViewManager) mLWelcome.getParent()).removeView(mLWelcome);
                                    mLWelcome = null;
                                }
                            }).setStartDelay(0).alpha(0).translationYBy(-15 * sD);
                        }
                    });

                    int animDur = animDuration;
                    int delayDur = 500;
                    if (orientationChanged) {
                        animDur = 0;
                        delayDur = 0;
                    }
                    mLWelcome.animate().setStartDelay(delayDur).setDuration(animDur).alpha(1).translationYBy(15 * sD);
                }
            }

            long time = Calendar.getInstance(TimeZone.getTimeZone(C.europeLondon)).getTimeInMillis();


            if (((float) (time - mPrefs.getLong(C.welcomeDate, 1)) / (24 * 60 * 60 * 1000) > 4
                    && mPrefs.getBoolean(C.feedbackFirstTime, true))
                    || ((float) (time - mPrefs.getLong(C.welcomeDate, 1)) / (24 * 60 * 60 * 1000) > 90)
                    && !mPrefs.getBoolean(C.feedbackDone, false)) {
                mPrefs.edit().putBoolean(C.feedbackFirstTime, false).apply();
                ViewStub v = findViewById(R.id.VSFeedback);
                if (v != null) {
                    mLFeedback = (LinearLayout) v.inflate();

                    int bottomMargin = 0;
                    if (Build.VERSION.SDK_INT >= 19)
                        bottomMargin = navigationBarHeight;
                    ((FrameLayout.LayoutParams) mLFeedback.getLayoutParams()).setMargins(0, 0, 0, (int) (35 * sD) + bottomMargin);

                    (mLFeedback.findViewById(R.id.BFeedbackYes)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mPrefs.edit().putBoolean(C.feedbackDone, true).apply();
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(C.marketDetails + getPackageName()))
                                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(res.getString(R.string.google_play_app_site)))
                                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
                            }
                            mLFeedback.animate().setDuration(animDuration).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    ((ViewManager) mLFeedback.getParent()).removeView(mLFeedback);
                                    mLFeedback = null;
                                }
                            }).setStartDelay(0).alpha(0).translationYBy(-15 * sD);
                        }
                    });
                    (mLFeedback.findViewById(R.id.BFeedbackDone)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mPrefs.edit().putBoolean(C.feedbackDone, true).apply();
                            Toast.makeText(ActivityMain.this, getString(R.string.w_main_feedback_done_thanks), Toast.LENGTH_SHORT).show();
                            mLFeedback.animate().setDuration(animDuration).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    ((ViewManager) mLFeedback.getParent()).removeView(mLFeedback);
                                    mLFeedback = null;
                                }
                            }).setStartDelay(0).alpha(0).translationYBy(-15 * sD);
                        }
                    });
                    (mLFeedback.findViewById(R.id.BFeedbackNo)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mLFeedback.animate().setDuration(animDuration).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    ((ViewManager) mLFeedback.getParent()).removeView(mLFeedback);
                                    mLFeedback = null;
                                }
                            }).setStartDelay(0).alpha(0).translationYBy(-15 * sD);
                            mPrefs.edit().putLong(C.welcomeDate, Calendar.getInstance(TimeZone.getTimeZone(C.europeLondon)).getTimeInMillis()).apply();
                            Toast.makeText(ActivityMain.this, getString(R.string.w_main_feedback_no_remind), Toast.LENGTH_LONG).show();
                        }
                    });

                    int animDur = animDuration;
                    int delayDur = 1000;
                    if (orientationChanged) {
                        animDur = 0;
                        delayDur = 0;
                    }
                    mLFeedback.animate().setStartDelay(delayDur).setDuration(animDur).alpha(1).translationYBy(15 * sD);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent serviceIntent = new Intent(getApplicationContext(), ReaderService.class);
        stopService(serviceIntent);
    }

    @Override
    public void onStart() {
        super.onStart();
        bindService(new Intent(this, ReaderService.class), mServiceConnection, 0);
    }

    @Override
    public void onStop() {
        super.onStop();
        unbindService(mServiceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setTextLabelMemory(TextView absolute, TextView percent, Collection<String> valuesCol) {

        List<String> values = new ArrayList<>(valuesCol);
        Log.i("values", valuesCol.toString());
        if (!values.isEmpty()) {
            Log.i("main", "set text");
            Log.i("main abs:", absolute.getText().toString());
            absolute.setText(mFormat.format(Integer.parseInt(values.get(values.size()-1))) + C.kB);
            Log.i("main abs:", absolute.getText().toString());
            percent.setText(mFormatPercent.format(Integer.parseInt(values.get(values.size()-1)) * 100 / (float) mSR.getMemTotal()) + C.percent);
        } else {

            Log.i("main", "values are empty!");
        }
    }
}