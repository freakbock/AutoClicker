package com.example.autoclicker;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class AutoClickService extends AccessibilityService {

    private Handler mHandler;
    private int mX;
    private int mY;

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread handlerThread = new HandlerThread("auto-handler");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    protected void onServiceConnected() {
        Log.d("AutoClickService", "Service connected");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service", "SERVICE STARTED");
        if (intent != null) {
            String action = intent.getStringExtra("action");
            if (action != null && action.equals("play")) {
                mX = intent.getIntExtra("x", 0);
                mY = intent.getIntExtra("y", 0);
                if (mRunnable == null) {
                    mRunnable = new IntervalRunnable();
                }
                mHandler.post(mRunnable);
                Toast.makeText(getBaseContext(), "Started", Toast.LENGTH_SHORT).show();
            } else if (action != null && action.equals("stop")) {
                mHandler.removeCallbacksAndMessages(null);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }

    private IntervalRunnable mRunnable;

    private class IntervalRunnable implements Runnable {
        @Override
        public void run() {
            Log.d("clicked", "click");
            Log.d("AutoClicker", mX + " | " + mY);
            GestureDescription gesture = createClickGesture(mX, mY);
            boolean isCalled = dispatchGesture(gesture, null, null);
            Log.d("GestureDispatch", "Gesture dispatched: " + isCalled);
            mHandler.postDelayed(this, 1000); // Запуск следующего касания через 1 секунду
        }
    }

    private GestureDescription createClickGesture(int x, int y) {
        Path clickPath = new Path();
        clickPath.moveTo(x, y);

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(clickPath, 0, 1);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(stroke);

        return gestureBuilder.build();
    }
}
