//package com.privateboinc;
//
//import android.Manifest;
//import android.animation.Animator;
//import android.animation.AnimatorListenerAdapter;
//import android.animation.ValueAnimator;
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.content.ActivityNotFoundException;
//import android.content.ComponentName;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.content.res.Configuration;
//import android.content.res.Resources;
//import android.graphics.Canvas;
//import android.graphics.SurfaceTexture;
//import android.graphics.drawable.BitmapDrawable;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.IBinder;
//import android.util.Log;
//import android.view.KeyCharacterMap;
//import android.view.KeyEvent;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.TextureView;
//import android.view.View;
//import android.view.ViewConfiguration;
//import android.view.ViewManager;
//import android.view.ViewStub;
//import android.view.ViewTreeObserver;
//import android.view.WindowManager;
//import android.widget.FrameLayout;
//import android.widget.LinearLayout;
//import android.widget.PopupWindow;
//import android.widget.TextView;
//import android.widget.Toast;
//import android.widget.ToggleButton;
//
//import androidx.core.app.ActivityCompat;
//
//import java.text.DecimalFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//import java.util.TimeZone;
//
//public class ActivityMainsaved extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {
//
//    private boolean cpuTotal, cpuAM,
//            memUsed, memAvailable, memFree, cached, threshold,
//            settingsShown, canvasLocked, orientationChanged;
//    private int intervalUpdate;
//    private int statusBarHeight;
//    private int navigationBarHeight;
//    private int animDuration = 200;
//    private int settingsHeight;
//    private int processesMode;
//    private int graphicMode;
//    private float sD;
//    private SharedPreferences mPrefs;
////    private FrameLayout mLSettings;
//    private FrameLayout mLGraphicSurface;
////    private LinearLayout mLTopBar;
////    private LinearLayout mLProcessContainer;
//    private LinearLayout mLFeedback;
//    private LinearLayout mLWelcome;
////    private LinearLayout mLCPUTotal;
////    private LinearLayout mLCPUAM;
////    private TextView mTVCPUTotalP;
////    private TextView mTVCPUAMP;
////    private TextView mTVMemoryAM;
//    private TextView mTVMemTotal;
//    private TextView mTVMemUsed;
//    private TextView mTVMemAvailable;
//    private TextView mTVMemFree;
//    private TextView mTVCached;
//    private TextView mTVThreshold;
//    private TextView mTVMemUsedP;
//    private TextView mTVMemAvailableP;
//    private TextView mTVMemFreeP;
//    private TextView mTVCachedP;
//    private TextView mTVThresholdP;
////    private ImageView mLButtonMenu, mLButtonRecord/*, mIVSettingsBG*/;
//    private DecimalFormat mFormat = new DecimalFormat("##,###,##0"), mFormatPercent = new DecimalFormat("##0.0"),
//            mFormatTime = new DecimalFormat("0.#");
//    private Resources res;
////    private Button mBChooseProcess, mBMemory, mBRemoveAll;
//    private ToggleButton mBHide;
//    private ViewGraphic mVG;
////    private SeekBar mSBRead;
//    private PopupWindow mPWMenu;
//    //	private RenderScript rs;
//    //	private ScriptIntrinsicBlur intrinsic;
//    private ReaderService mSR;
////    private List<Map<String, Object>> mListSelected;
//    private Intent tempIntent;
//    private Handler mHandler = new Handler(), mHandlerVG = new Handler();
//    private Thread mThread;
//    private Runnable drawRunnable = new Runnable() {
//        @SuppressLint("NewApi")
//        @Override
//        public void run() {
//            mHandler.postDelayed(this, intervalUpdate);
//            if (mSR != null) { // finish() could have been called from the BroadcastReceiver
//                mHandlerVG.post(drawRunnableGraphic);
//
//                setTextLabelMemory(mTVMemUsed, mTVMemUsedP, mSR.getmMemUsed());
//                setTextLabelMemory(mTVMemAvailable, mTVMemAvailableP, mSR.getmMemAvailable());
//                setTextLabelMemory(mTVMemFree, mTVMemFreeP, mSR.getmMemFree());
//                setTextLabelMemory(mTVCached, mTVCachedP, mSR.getmCached());
//                setTextLabelMemory(mTVThreshold, mTVThresholdP, mSR.getThreshold());
//
////                for (int n = 0; n < mLProcessContainer.getChildCount(); ++n) {
////                    LinearLayout l = (LinearLayout) mLProcessContainer.getChildAt(n);
////                    setTextLabelMemoryProcesses(l);
////                }
//            }
//        }
//    };
//    private Runnable drawRunnableGraphic = new Runnable() { // http://stackoverflow.com/questions/18856376/android-why-cant-i-create-a-handler-in-new-thread
//        @Override
//        public void run() {
//            mThread = new Thread() {
//                @Override
//                public void run() {
//                    Canvas canvas;
//                    if (!canvasLocked) { // http://stackoverflow.com/questions/9792446/android-java-lang-illegalargumentexception
//                        canvas = mVG.lockCanvas();
//                        if (canvas != null) {
//                            canvasLocked = true;
//                            mVG.onDrawCustomised(canvas, mThread);
//
//                            // https://github.com/AntonioRedondo/AnotherMonitor/issues/1
//                            // http://stackoverflow.com/questions/23893813/canvas-restore-causing-underflow-exception-in-very-rare-cases
//                            try {
//                                mVG.unlockCanvasAndPost(canvas);
//                            } catch (IllegalStateException e) {
//                                Log.w("Activity main: ", e.getMessage());
//                            }
//
//                            canvasLocked = false;
//                        }
//                    }
//                }
//            };
//            mThread.start();
//        }
//    };
//
//    private ServiceConnection mServiceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            mSR = ((ReaderService.ReaderServiceBinder) service).getService();
//
//            mVG.setService(mSR);
//
//
//            mTVMemTotal.setText(mFormat.format(mSR.getmMemTotal()) + C.kB);
//
//            mHandler.removeCallbacks(drawRunnable);
//            mHandler.post(drawRunnable);
//
//            // When on ActivityProcesses the screen is rotated, ActivityMain is destroyed and back is pressed from ActivityProcesses
//            // mSR isn't ready before onActivityResult() is called. So the Intent is saved till mSR is ready.
//            if (tempIntent != null) {
//                tempIntent.putExtra(C.screenRotated, true);
//                onActivityResult(1, 1, tempIntent);
//                tempIntent = null;
//            } else onActivityResult(1, 1, null);
//
////            if (Build.VERSION.SDK_INT >= 16) {
////                mLProcessContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
////                    @SuppressWarnings("deprecation")
////                    @Override
////                    public void onGlobalLayout() {
////                        mLProcessContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
////                        LayoutTransition lt = new LayoutTransition();
////                        lt.enableTransitionType(LayoutTransition.APPEARING);
////                        lt.enableTransitionType(LayoutTransition.DISAPPEARING);
////                        lt.enableTransitionType(LayoutTransition.CHANGING);
////                        mLProcessContainer.setLayoutTransition(lt);
////                        LayoutTransition lt2 = new LayoutTransition();
////                        lt2.enableTransitionType(LayoutTransition.CHANGING);
////                        lt2.setStartDelay(LayoutTransition.CHANGING, 300);
////                        ((LinearLayout) mLProcessContainer.getParent()).setLayoutTransition(lt2);
////                    }
////                });
////            }
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName className) {
//            mSR = null;
//        }
//    };
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        Intent serviceIntent = new Intent(getApplicationContext(), ReaderService.class);
//        startService(serviceIntent);
//        mVG = (ViewGraphic) findViewById(R.id.ANGraphic);
//        setContentView(R.layout.activity_main);
//        {
//            super.onCreate(savedInstanceState);
//            startService(new Intent(this, ReaderService.class));
//            setContentView(R.layout.activity_main);
//
//            mPrefs = getSharedPreferences(getString(R.string.app_name) + C.prefs, MODE_PRIVATE);
//            int intervalRead = mPrefs.getInt(C.intervalRead, C.defaultIntervalUpdate);
//            intervalUpdate = mPrefs.getInt(C.intervalUpdate, C.defaultIntervalUpdate);
//            int intervalWidth = mPrefs.getInt(C.intervalWidth, C.defaultIntervalWidth);
//
//            cpuTotal = mPrefs.getBoolean(C.cpuTotal, true);
//            cpuAM = mPrefs.getBoolean(C.cpuAM, true);
//
//            memUsed = mPrefs.getBoolean(C.memUsed, true);
//            memAvailable = mPrefs.getBoolean(C.memAvailable, true);
//            memFree = mPrefs.getBoolean(C.memFree, false);
//            cached = mPrefs.getBoolean(C.cached, false);
//            threshold = mPrefs.getBoolean(C.threshold, true);
//
//            res = getResources();
//            sD = res.getDisplayMetrics().density;
//            //		sWidth = res.getDisplayMetrics().widthPixels;
//            //		sHeight = res.getDisplayMetrics().heightPixels;
//            sD = res.getDisplayMetrics().density;
//            int orientation = res.getConfiguration().orientation;
//            statusBarHeight = res.getDimensionPixelSize(res.getIdentifier(C.sbh, C.dimen, C.android));
//
////            final SeekBar mSBWidth = (SeekBar) findViewById(R.id.SBIntervalWidth);
//            if (savedInstanceState != null && !savedInstanceState.isEmpty() && savedInstanceState.getInt(C.orientation) != orientation)
//                orientationChanged = true;
//
//
//            mVG = (ViewGraphic) findViewById(R.id.ANGraphic);
//
//            graphicMode = mPrefs.getInt(C.graphicMode, C.graphicModeShowMemory);
//
//
//            processesMode = mPrefs.getInt(C.processesMode, C.processesModeShowCPU);
////            mBMemory = (Button) findViewById(R.id.BMemory);
////            mBMemory.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////                    processesMode = processesMode == C.processesModeShowCPU ? C.processesModeShowMemory : C.processesModeShowCPU;
////                    mPrefs.edit().putInt(C.processesMode, processesMode).apply();
////                    mBMemory.setText(processesMode == 0 ? getString(R.string.w_main_memory) : getString(R.string.p_cpuusage));
////                    mVG.setProcessesMode(processesMode);
////                    mHandlerVG.post(drawRunnableGraphic);
////                    mHandler.removeCallbacks(drawRunnable);
////                    mHandler.post(drawRunnable);
////                }
////            });
////            mBMemory.setText(processesMode == 0 ? getString(R.string.w_main_memory) : getString(R.string.p_cpuusage));
//
//
////            mLTopBar = (LinearLayout) findViewById(R.id.LTopBar);
//            mLGraphicSurface = (FrameLayout) findViewById(R.id.LGraphicButton);
//
//            if (Build.VERSION.SDK_INT >= 19) {
//                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//
//                float sSW = res.getConfiguration().smallestScreenWidthDp;
//
//                if (!ViewConfiguration.get(this).hasPermanentMenuKey() && !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME)
//                        && (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || sSW > 560)) {
//                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
//                    navigationBarHeight = res.getDimensionPixelSize(res.getIdentifier(C.nbh, C.dimen, C.android));
//                    if (navigationBarHeight == 0)
//                        navigationBarHeight = (int) (48 * sD);
//
//                    FrameLayout nb = (FrameLayout) findViewById(R.id.LNavigationBar);
//                    nb.setVisibility(View.VISIBLE);
//                    ((FrameLayout.LayoutParams) nb.getLayoutParams()).height = navigationBarHeight;
//                    ((FrameLayout.LayoutParams) mVG.getLayoutParams()).setMargins(0, 0, 0, navigationBarHeight);
//                    ((FrameLayout.LayoutParams) mLGraphicSurface.getLayoutParams()).setMargins(0, 0, 0, navigationBarHeight);
//
////                    int paddingTop = mSBWidth.getPaddingTop();
////                    int paddingBottom = mSBWidth.getPaddingBottom();
////                    int paddingLeft = mSBWidth.getPaddingLeft();
////                    int paddingRight = mSBWidth.getPaddingRight();
////                    mSBWidth.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom + navigationBarHeight);
//                }
//
////                int paddingTop = mLTopBar.getPaddingTop();
////                int paddingBottom = mLTopBar.getPaddingBottom();
////                int paddingLeft = mLTopBar.getPaddingLeft();
////                int paddingRight = mLTopBar.getPaddingRight();
////                mLTopBar.setPadding(paddingLeft, paddingTop + statusBarHeight, paddingRight, paddingBottom);
//            }
//
//            LinearLayout mLParent = (LinearLayout) findViewById(R.id.LParent);
//
////            mLButtonMenu = (ImageView) findViewById(R.id.LButtonMenu);
////
//            //		if (!ViewConfiguration.get(this).hasPermanentMenuKey()) {
////            mLButtonMenu.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////                    mPWMenu.setAnimationStyle(R.style.Animations_PopDownMenu);
////                    if (Build.VERSION.SDK_INT < 19)
////                        mPWMenu.showAtLocation(mLTopBar, Gravity.TOP | Gravity.RIGHT, 0, mLTopBar.getHeight() + statusBarHeight);
////                    else
////                        mPWMenu.showAtLocation(mLTopBar, Gravity.TOP | Gravity.RIGHT, 0, mLTopBar.getHeight());
////                }
////            });
//            //		I came across with a Vodafone Ultra phone which had the Menu button implemented as an apps Navigation bar button long-click.
//            //		With the below code and with these rare phones the AnotherMonitor menu button was hidden. Not anymore.
///*		} else {
//			mLButtonMenu.setVisibility(View.GONE);
//			int paddingTop = mLTopBar.getPaddingTop();
//			int paddingBottom = mLTopBar.getPaddingBottom();
//			int paddingLeft = mLTopBar.getPaddingLeft();
//			int paddingRight = mLTopBar.getPaddingRight();
//			mLTopBar.setPadding(paddingLeft, paddingTop, paddingRight + (int) (14*sD), paddingBottom);
//		}*/
//            LinearLayout mLMenu = (LinearLayout) getLayoutInflater().inflate(R.layout.layer_menu, null);
//            mLMenu.setFocusableInTouchMode(true);
//
//            mPWMenu = new PopupWindow(mLMenu, (int) (260 * sD), WindowManager.LayoutParams.WRAP_CONTENT, true);
//            mPWMenu.setAnimationStyle(R.style.Animations_PopDownMenu);
//            mPWMenu.setBackgroundDrawable(new BitmapDrawable()); // Without this line ACTION_OUTSIDE events are not triggered.
//            mPWMenu.getContentView().setOnKeyListener(new View.OnKeyListener() {
//                @Override
//                public boolean onKey(View v, int keyCode, KeyEvent event) {
//                    if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_UP) {
//                        mPWMenu.dismiss();
//                        return true;
//                    }
//                    return false;
//                }
//            });
//
//            mLMenu.findViewById(R.id.LHelp).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mPWMenu.dismiss();
//                    startActivity(new Intent(ActivityMainsaved.this, ActivityHelp.class));
//                }
//            });
//
//            mLMenu.findViewById(R.id.LAbout).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mPWMenu.dismiss();
//                    startActivity(new Intent(ActivityMainsaved.this, ActivityAbout.class));
//                }
//            });
//
//            mLMenu.findViewById(R.id.LClose).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mSR.stopSelf();
//                    finish();
//                }
//            });
//
////            mLProcessContainer = (LinearLayout) findViewById(R.id.LProcessContainer);
//
////            mLCPUTotal = (LinearLayout) findViewById(R.id.LCPUTotal);
////            mLCPUTotal.setTag(C.cpuTotal);
////            mLCPUTotal.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////                    switchParameter(cpuTotal = !cpuTotal, mLCPUTotal);
////                }
////            });
////            mLCPUAM = (LinearLayout) findViewById(R.id.LCPUAM);
////            mLCPUAM.setTag(C.cpuAM);
////            mLCPUAM.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////                    switchParameter(cpuAM = !cpuAM, mLCPUAM);
////                }
////            });
////            ((TextView) ((LinearLayout) mLCPUAM.getChildAt(2)).getChildAt(1)).setText("Pid: " + Process.myPid());
//
//            LinearLayout mLMemUsed = (LinearLayout) findViewById(R.id.LMemUsed);
//            mLMemUsed.setTag(C.memUsed);
////            mLMemUsed.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////                    switchParameter(memUsed = !memUsed, mLMemUsed);
////                }
////            });
//            LinearLayout mLMemAvailable = (LinearLayout) findViewById(R.id.LMemAvailable);
//            mLMemAvailable.setTag(C.memAvailable);
////            mLMemAvailable.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////                    switchParameter(memAvailable = !memAvailable, mLMemAvailable);
////                }
////            });
//            LinearLayout mLMemFree = (LinearLayout) findViewById(R.id.LMemFree);
//            mLMemFree.setTag(C.memFree);
////            mLMemFree.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////                    switchParameter(memFree = !memFree, mLMemFree);
////                }
////            });
//            LinearLayout mLCached = (LinearLayout) findViewById(R.id.LCached);
//            mLCached.setTag(C.cached);
////            mLCached.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////                    switchParameter(cached = !cached, mLCached);
////                }
////            });
//            LinearLayout mLThreshold = (LinearLayout) findViewById(R.id.LThreshold);
//            mLThreshold.setTag(C.threshold);
////            mLThreshold.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////                    switchParameter(threshold = !threshold, mLThreshold);
////                }
////            });
//
////            mTVCPUTotalP = (TextView) findViewById(R.id.TVCPUTotalP);
////            mTVCPUAMP = (TextView) findViewById(R.id.TVCPUAMP);
////            mTVMemoryAM = (TextView) findViewById(R.id.TVMemoryAM);
//            mTVMemTotal = (TextView) findViewById(R.id.TVMemTotal);
//            mTVMemUsed = (TextView) findViewById(R.id.TVMemUsed);
//            mTVMemUsedP = (TextView) findViewById(R.id.TVMemUsedP);
//            mTVMemAvailable = (TextView) findViewById(R.id.TVMemAvailable);
//            mTVMemAvailableP = (TextView) findViewById(R.id.TVMemAvailableP);
//            mTVMemFree = (TextView) findViewById(R.id.TVMemFree);
//            mTVMemFreeP = (TextView) findViewById(R.id.TVMemFreeP);
//            mTVCached = (TextView) findViewById(R.id.TVCached);
//            mTVCachedP = (TextView) findViewById(R.id.TVCachedP);
//            mTVThreshold = (TextView) findViewById(R.id.TVThreshold);
//            mTVThresholdP = (TextView) findViewById(R.id.TVThresholdP);
//
//            //		mIVSettingsBG = (ImageView) findViewById(R.id.IVSettingsBG);
//
////            mLSettings = (FrameLayout) findViewById(R.id.LSettings);
////            mLSettings.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
////                @Override
////                public void onGlobalLayout() {
////                    mLSettings.getViewTreeObserver().removeGlobalOnLayoutListener(this);
////                    settingsHeight = mLSettings.getHeight();
////                    mLSettings.getLayoutParams().height = 0;
////                    //				mIVSettingsBG.getLayoutParams().height = settingsHeight;
////                }
////            });
//
//            mLGraphicSurface.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    showSettings();
//                }
//            });
//            mLGraphicSurface.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View arg0) {
//                    Toast.makeText(ActivityMainsaved.this, getString(R.string.menu_settings_description), Toast.LENGTH_SHORT).show();
//                    return true;
//                }
//            });
//
//            mVG.setOpaque(false); // http://stackoverflow.com/questions/18993355/semi-transparent-textureviews-not-working
//
//            // http://stackoverflow.com/questions/12688409/android-textureview-canvas-drawing-problems
//            // https://groups.google.com/forum/?fromgroups=#!topic/android-developers/_Ogjc8sozpA
//            // http://pastebin.com/J4uDgrZ8
//            mVG.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
//                @Override
//                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
///*				if (drawThread == null) {
//					drawThread = new Thread(drawRunnable3, C.drawThread);
//				}
//				drawThread.start();*/
///*				mVG.getSurfaceTexture().setOnFrameAvailableListener( new SurfaceTexture.OnFrameAvailableListener() {
//					@Override
//					public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//						updateLayer();
//						invalidate();
//					}
//				});*/
//
//                    //				mHandlerVG.post(drawRunnableGraphic);
//                }
//
//                @Override
//                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//                }
//
//                @Override
//                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
///*				try {
//					drawThread.interrupt();
//					drawThread = null;
//				} catch (Exception e) {
//					e.printStackTrace();
//				}*/
//                    return true;
//                }
//
//                @Override
//                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//                }
//
//            });
//            mVG.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    mVG.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mLGraphicSurface.getLayoutParams();
//                    //				xLeft = (int) (getWidth()*0.14);
//                    //				xRight = (int) (getWidth()*0.94);
//                    //				yTop = (int) (getHeight()*0.1);
//                    //				yBottom = (int) (getHeight()*0.88);
//                    lp.setMargins((int) (mVG.getWidth() * 0.14), (int) (mVG.getHeight() * 0.1), (int) (mVG.getWidth() * 0.06), (int) (mVG.getHeight() * 0.12) + navigationBarHeight);
//                }
//            });
//
////            mBChooseProcess = (Button) findViewById(R.id.BChooseProcess);
////            mBChooseProcess.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////                    Intent i = new Intent(ActivityMain.this, ActivityProcesses.class);
////                    i.putExtra(C.listSelected, (Serializable) mListSelected);
////                    startActivityForResult(i, 1);
////                }
////            });
////            mBRemoveAll = (Button) findViewById(R.id.BRemoveAll);
////            mBRemoveAll.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////                    mListSelected.clear(); // This also updates the List on ServiceReader because it is poiting to the same object
////                    mLProcessContainer.removeAllViews();
////                    mHandlerVG.post(drawRunnableGraphic);
////                    mBRemoveAll.animate().setDuration(300).alpha(0).setListener(new AnimatorListenerAdapter() {
////                        @Override
////                        public void onAnimationEnd(Animator animation) {
////                            mBRemoveAll.setVisibility(View.GONE);
////                        }
////                    });
////                }
////            });
//
////            final TextView mTVIntervalRead = (TextView) findViewById(R.id.TVIntervalRead);
////            mTVIntervalRead.setText(getString(R.string.interval_read) + " " + mFormatTime.format(intervalRead / (float) 1000) + " s");
////            final TextView mTVIntervalUpdate = (TextView) findViewById(R.id.TVIntervalUpdate);
////            mTVIntervalUpdate.setText(getString(R.string.interval_update) + " " + mFormatTime.format(intervalUpdate / (float) 1000) + " s");
////            final TextView mTVIntervalWidth = (TextView) findViewById(R.id.TVIntervalWidth);
////            mTVIntervalWidth.setText(getString(R.string.interval_width) + " " + intervalWidth + " dp");
////
////            mSBRead = (SeekBar) findViewById(R.id.SBIntervalRead);
//            int t = 0;
//            switch (intervalRead) {
//                case 500:
//                    t = 0;
//                    break;
//                case 1000:
//                    t = 1;
//                    break;
//                case 2000:
//                    t = 2;
//                    break;
//                case 4000:
//                    t = 4;
//            }
////            mSBRead.setProgress(t);
////            mSBRead.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
////                @Override
////                public void onStopTrackingTouch(SeekBar seekBar) {
////
////                }
////
////                @Override
////                public void onStartTrackingTouch(SeekBar seekBar) {
////
////                }
////
////                @Override
////                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
////                    seekBar.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
////                    int t = 0;
////                    switch (mSBRead.getProgress()) {
////                        case 0:
////                            t = 500;
////                            break;
////                        case 1:
////                            t = 1000;
////                            break;
////                        case 2:
////                            t = 2000;
////                            break;
////                        case 3:
////                            t = 4000;
////                    }
//////                    mTVIntervalRead.setText(getString(R.string.interval_read) + " " + mFormatTime.format(t / (float) 1000) + " s");
////                }
////            });
//
////            final SeekBar mSBUpdate = (SeekBar) findViewById(R.id.SBIntervalUpdate);
//            t = 0;
//            switch (intervalUpdate) {
//                case 500:
//                    t = 0;
//                    break;
//                case 1000:
//                    t = 1;
//                    break;
//                case 2000:
//                    t = 2;
//                    break;
//                case 4000:
//                    t = 3;
//            }
////            mSBUpdate.setProgress(t);
////            mSBUpdate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
////                @Override
////                public void onStopTrackingTouch(SeekBar seekBar) {
////                }
////
////                @Override
////                public void onStartTrackingTouch(SeekBar seekBar) {
////                }
////
////                @Override
////                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
////                    seekBar.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
////                    int t = 0;
////                    switch (mSBUpdate.getProgress()) {
////                        case 0:
////                            t = 500;
////                            break;
////                        case 1:
////                            t = 1000;
////                            break;
////                        case 2:
////                            t = 2000;
////                            break;
////                        case 3:
////                            t = 4000;
////                    }
////                    mTVIntervalUpdate.setText(getString(R.string.interval_update) + " " + mFormatTime.format(t / (float) 1000) + " s");
////                }
////            });
//
//            t = 0;
//            switch (intervalWidth) {
//                case 1:
//                    t = 0;
//                    break;
//                case 2:
//                    t = 1;
//                    break;
//                case 5:
//                    t = 2;
//                    break;
//                case 10:
//                    t = 4;
//            }
////            mSBWidth.setProgress(t);
////            mSBWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
////                @Override
////                public void onStopTrackingTouch(SeekBar seekBar) {
////                }
////
////                @Override
////                public void onStartTrackingTouch(SeekBar seekBar) {
////                }
////
////                @Override
////                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
////                    seekBar.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
////                    int t = 0;
////                    switch (mSBWidth.getProgress()) {
////                        case 0:
////                            t = 1;
////                            break;
////                        case 1:
////                            t = 2;
////                            break;
////                        case 2:
////                            t = 5;
////                            break;
////                        case 3:
////                            t = 10;
////                    }
////                    mTVIntervalWidth.setText(getString(R.string.interval_width) + " " + t + " dp");
////                }
////            });
////
////            FrameLayout mCloseSettings = (FrameLayout) findViewById(R.id.LOK);
////            mCloseSettings.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////                    hideSettings();
////
////                    int intervalWidth = 0, intervalRead = 0, intervalUpdate = 0;
////
////                    switch (mSBRead.getProgress()) {
////                        case 0:
////                            intervalRead = 500;
////                            break;
////                        case 1:
////                            intervalRead = 1000;
////                            break;
////                        case 2:
////                            intervalRead = 2000;
////                            break;
////                        case 3:
////                            intervalRead = 4000;
////                    }
////
////                    switch (mSBUpdate.getProgress()) {
////                        case 0:
////                            intervalUpdate = 500;
////                            break;
////                        case 1:
////                            intervalUpdate = 1000;
////                            break;
////                        case 2:
////                            intervalUpdate = 2000;
////                            break;
////                        case 3:
////                            intervalUpdate = 4000;
////                    }
////
////                    switch (mSBWidth.getProgress()) {
////                        case 0:
////                            intervalWidth = 1;
////                            break;
////                        case 1:
////                            intervalWidth = 2;
////                            break;
////                        case 2:
////                            intervalWidth = 5;
////                            break;
////                        case 3:
////                            intervalWidth = 10;
////                    }
////
////                    if (intervalRead > intervalUpdate) {
////                        intervalUpdate = intervalRead;
////                        int t = 0;
////                        switch (intervalUpdate) {
////                            case 500:
////                                t = 0;
////                                break;
////                            case 1000:
////                                t = 1;
////                                break;
////                            case 2000:
////                                t = 2;
////                                break;
////                            case 4000:
////                                t = 3;
////                        }
////                        mSBUpdate.setProgress(t);
////                    }
////
////                    if (ActivityMain.this.intervalRead != intervalRead) {
////                        if (mListSelected != null && !mListSelected.isEmpty())
////                            for (Map<String, Object> process : mListSelected) {
////                                process.put(C.pFinalValue, new ArrayList<Float>());
////                                process.put(C.pTPD, new ArrayList<Integer>());
////                            }
////
////                        mSR.getmMemUsed().clear();
////                        mSR.getmMemAvailable().clear();
////                        mSR.getmMemFree().clear();
////                        mSR.getmCached().clear();
////                        mSR.getThreshold().clear();
////                    }
////
////                    ActivityMain.this.intervalRead = intervalRead;
////                    ActivityMain.this.intervalUpdate = intervalUpdate;
////                    ActivityMain.this.intervalWidth = intervalWidth;
////
////                    //mSR.setIntervals(intervalRead, intervalUpdate, intervalWidth);
////                    mVG.calculateInnerVariables();
////                    mHandlerVG.post(drawRunnableGraphic);
////                    mHandler.removeCallbacks(drawRunnable);
////                    mHandler.post(drawRunnable);
////                    mPrefs.edit()
////                            .putInt(C.intervalRead, intervalRead)
////                            .putInt(C.intervalUpdate, intervalUpdate)
////                            .putInt(C.intervalWidth, intervalWidth)
////                            .apply();
////                }
////            });
//
//            if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, C.storagePermission);
//
//            if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
//                processesMode = savedInstanceState.getInt(C.processesMode);
//
//                canvasLocked = savedInstanceState.getBoolean(C.canvasLocked);
//                settingsShown = savedInstanceState.getBoolean(C.settingsShown);
////                if (settingsShown)
////                    mLSettings.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
////                        @Override
////                        public void onGlobalLayout() {
////                            mLSettings.getViewTreeObserver().removeGlobalOnLayoutListener(this);
////                            mLSettings.getLayoutParams().height = settingsHeight;
////                        }
////                    });
////                if (savedInstanceState.getBoolean(C.menuShown))
////                    mLTopBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
////                        @Override
////                        public void onGlobalLayout() {
////                            mLTopBar.getViewTreeObserver().removeGlobalOnLayoutListener(this);
////                            mPWMenu.showAtLocation(mLTopBar, Gravity.TOP | Gravity.RIGHT, 0, mLTopBar.getHeight());
////                        }
////                    });
//            }
//
//            //		if (true) {
//            if (mPrefs.getBoolean(C.welcome, true)) {
//                mPrefs.edit().putLong(C.welcomeDate, Calendar.getInstance(TimeZone.getTimeZone(C.europeLondon)).getTimeInMillis()).apply();
//                ViewStub v = (ViewStub) findViewById(R.id.VSWelcome);
//                if (v != null) { // This is to avoid a null pointer when the second time this code is executed (findViewById() returns the view only once)
//                    mLWelcome = (LinearLayout) v.inflate();
//
//                    int bottomMargin = 0;
//                    if (Build.VERSION.SDK_INT >= 19)
//                        bottomMargin = navigationBarHeight;
//                    ((FrameLayout.LayoutParams) mLWelcome.getLayoutParams()).setMargins(0, 0, 0, (int) (35 * sD) + bottomMargin);
//
//                    (mLWelcome.findViewById(R.id.BHint)).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            mPrefs.edit().putBoolean(C.welcome, false).apply();
//                            mLWelcome.animate().setDuration(animDuration).setListener(new AnimatorListenerAdapter() {
//                                @Override
//                                public void onAnimationEnd(Animator animation) {
//                                    ((ViewManager) mLWelcome.getParent()).removeView(mLWelcome);
//                                    mLWelcome = null;
//                                }
//                            }).setStartDelay(0).alpha(0).translationYBy(-15 * sD);
//                        }
//                    });
//
//                    int animDur = animDuration;
//                    int delayDur = 500;
//                    if (orientationChanged) {
//                        animDur = 0;
//                        delayDur = 0;
//                    }
//                    mLWelcome.animate().setStartDelay(delayDur).setDuration(animDur).alpha(1).translationYBy(15 * sD);
//                }
//            }
//
//            long time = Calendar.getInstance(TimeZone.getTimeZone(C.europeLondon)).getTimeInMillis();
//
//            //		if (true) {
//            if (((float) (time - mPrefs.getLong(C.welcomeDate, 1)) / (24 * 60 * 60 * 1000) > 4
//                    && mPrefs.getBoolean(C.feedbackFirstTime, true))
//                    || ((float) (time - mPrefs.getLong(C.welcomeDate, 1)) / (24 * 60 * 60 * 1000) > 90)
//                    && !mPrefs.getBoolean(C.feedbackDone, false)) {
//                mPrefs.edit().putBoolean(C.feedbackFirstTime, false).apply();
//                ViewStub v = (ViewStub) findViewById(R.id.VSFeedback);
//                if (v != null) { // This is to avoid a null pointer when the second time this code is executed (findViewById() returns the view only once)
//                    mLFeedback = (LinearLayout) v.inflate();
//
//                    int bottomMargin = 0;
//                    if (Build.VERSION.SDK_INT >= 19)
//                        bottomMargin = navigationBarHeight;
//                    ((FrameLayout.LayoutParams) mLFeedback.getLayoutParams()).setMargins(0, 0, 0, (int) (35 * sD) + bottomMargin);
//
//                    (mLFeedback.findViewById(R.id.BFeedbackYes)).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            mPrefs.edit().putBoolean(C.feedbackDone, true).apply();
//                            try {
//                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(C.marketDetails + getPackageName()))
//                                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
//                            } catch (ActivityNotFoundException e) {
//                                e.printStackTrace();
//                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(res.getString(R.string.google_play_app_site)))
//                                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
//                            }
//                            mLFeedback.animate().setDuration(animDuration).setListener(new AnimatorListenerAdapter() {
//                                @Override
//                                public void onAnimationEnd(Animator animation) {
//                                    ((ViewManager) mLFeedback.getParent()).removeView(mLFeedback);
//                                    mLFeedback = null;
//                                }
//                            }).setStartDelay(0).alpha(0).translationYBy(-15 * sD);
//                        }
//                    });
//                    (mLFeedback.findViewById(R.id.BFeedbackDone)).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            mPrefs.edit().putBoolean(C.feedbackDone, true).apply();
//                            Toast.makeText(ActivityMainsaved.this, getString(R.string.w_main_feedback_done_thanks), Toast.LENGTH_SHORT).show();
//                            mLFeedback.animate().setDuration(animDuration).setListener(new AnimatorListenerAdapter() {
//                                @Override
//                                public void onAnimationEnd(Animator animation) {
//                                    ((ViewManager) mLFeedback.getParent()).removeView(mLFeedback);
//                                    mLFeedback = null;
//                                }
//                            }).setStartDelay(0).alpha(0).translationYBy(-15 * sD);
//                        }
//                    });
//                    (mLFeedback.findViewById(R.id.BFeedbackNo)).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            mLFeedback.animate().setDuration(animDuration).setListener(new AnimatorListenerAdapter() {
//                                @Override
//                                public void onAnimationEnd(Animator animation) {
//                                    ((ViewManager) mLFeedback.getParent()).removeView(mLFeedback);
//                                    mLFeedback = null;
//                                }
//                            }).setStartDelay(0).alpha(0).translationYBy(-15 * sD);
//                            mPrefs.edit().putLong(C.welcomeDate, Calendar.getInstance(TimeZone.getTimeZone(C.europeLondon)).getTimeInMillis()).apply();
//                            Toast.makeText(ActivityMainsaved.this, getString(R.string.w_main_feedback_no_remind), Toast.LENGTH_LONG).show();
//                        }
//                    });
//
//                    int animDur = animDuration;
//                    int delayDur = 1000;
//                    if (orientationChanged) {
//                        animDur = 0;
//                        delayDur = 0;
//                    }
//                    mLFeedback.animate().setStartDelay(delayDur).setDuration(animDur).alpha(1).translationYBy(15 * sD);
//                }
//            }
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Intent serviceIntent = new Intent(getApplicationContext(), ReaderService.class);
//        stopService(serviceIntent);
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        bindService(new Intent(this, ReaderService.class), mServiceConnection, 0);
////        registerReceiver(receiverSetIconRecord, new IntentFilter(C.actionSetIconRecord));
////        registerReceiver(receiverDeadProcess, new IntentFilter(C.actionDeadProcess));
////        registerReceiver(receiverFinish, new IntentFilter(C.actionFinishActivity));
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        unbindService(mServiceConnection);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        //getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void setTextLabelMemory(TextView absolute, TextView percent, Collection<String> valuesCol) {
//        List<String> values = new ArrayList<>(valuesCol);
//        if (!values.isEmpty()) {
//            absolute.setText(mFormat.format(Integer.parseInt(values.get(0))) + C.kB);
//            percent.setText(mFormatPercent.format(Integer.parseInt(values.get(0)) * 100 / (float) mSR.getmMemTotal()) + C.percent);
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    private void setTextLabelMemoryProcesses(LinearLayout l) {
//        TextView tv = (TextView) l.findViewById(R.id.TVpAbsolute);
//        if (processesMode == C.processesModeShowCPU)
//            tv.setVisibility(View.INVISIBLE);
//        else {
//            Map<String, Object> entry = (Map<String, Object>) l.getTag();
//            if (entry != null
//                    && entry.get(C.pTPD) != null && !((List<String>) entry.get(C.pTPD)).isEmpty()
//                    && entry.get(C.pDead) == null) {
//                tv.setVisibility(View.VISIBLE);
//                tv.setText(String.format("%s%s",
//                        mFormat.format(((List<String>) entry.get(C.pTPD)).get(0)), C.kB));
//            }
//        }
//    }
//    private void showSettings() {
//        settingsShown = true;
//        mLGraphicSurface.setEnabled(false);
//
//        // By changing the panel position in this way you can keep the background height unchanged but cropped and attached to the top of the panel.
//        // This is important if you're using an image as background.
//        //		mLSettings.animate().setDuration(animTime).setInterpolator(new AccelerateDecelerateInterpolator()).translationY(0);
//
//        // In this way the background height will progressively be bigger, it will enlarge from 0 to the real size.
//        // Now this doesn't matter because the background is a solid color.
//        ValueAnimator va = ValueAnimator.ofInt(0, settingsHeight);
//        va.setDuration(animDuration);
////        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
////            @Override
////            public void onAnimationUpdate(ValueAnimator animation) {
////                Integer value = (Integer) animation.getAnimatedValue();
//////                mLSettings.getLayoutParams().height = value.intValue();
//////                mLSettings.requestLayout();
////            }
////        });
//        va.start();
//    }
//
//
////    private void hideSettings() {
////        settingsShown = false;
////        mLGraphicSurface.setEnabled(true);
////        //		mLSettings.animate().setDuration(animTime).setInterpolator(new AccelerateDecelerateInterpolator()).y(sHeight + navigationBarHeight);
////        ValueAnimator va = ValueAnimator.ofInt(settingsHeight, 0);
////        va.setDuration(animDuration);
//////        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//////            @Override
//////            public void onAnimationUpdate(ValueAnimator animation) {
//////                Integer value = (Integer) animation.getAnimatedValue();
////////                mLSettings.getLayoutParams().height = value.intValue();
////////                mLSettings.requestLayout();
//////            }
//////        });
////        va.start();
////    }
//}
