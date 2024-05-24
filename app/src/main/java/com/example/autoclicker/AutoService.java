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

public class AutoService extends AccessibilityService {

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
        Log.d("AutoService", "Service connected");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service","SERVICE STARTED");
        if(intent!=null){
            String action = intent.getStringExtra("action");
            if (action.equals("play")) {
                mX = intent.getIntExtra("x", 0);
                mY = intent.getIntExtra("y", 0);
                if (mRunnable == null) {
                    mRunnable = new IntervalRunnable();
                }
                mHandler.postDelayed(mRunnable, 1000);
                mHandler.post(mRunnable);
                Toast.makeText(getBaseContext(), "Started", Toast.LENGTH_SHORT).show();
            }
            else if(action.equals("stop")){
                mHandler.removeCallbacksAndMessages(null);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

//    //@RequiresApi(api = Build.VERSION_CODES.N)
//    private void playTap(int x, int y) {
//
//        if(x< 0 || y <0){
//            Log.d("AutoClicker", "Координаты отрицательны");
//            return;
//        }
//
//        boolean isCalled = dispatchGesture(gestureDescription(0, 10, x, y), null, null);
//        System.out.println(isCalled);
//
////        Path swipePath = new Path();
////        swipePath.moveTo(x, y);
////        swipePath.lineTo(x, y);
////        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
////        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 10));
////        //dispatchGesture(gestureBuilder.build(), null, null);
////        dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
////            @Override
////            public void onCompleted(GestureDescription gestureDescription) {
////                Log.d("Gesture Completed","Gesture Completed");
////                super.onCompleted(gestureDescription);
////                mHandler.postDelayed(mRunnable, 1);
////                mHandler.post(mRunnable);
////            }
////
////            @Override
////            public void onCancelled(GestureDescription gestureDescription) {
////                Log.d("Gesture Cancelled","Gesture Cancelled");
////                super.onCancelled(gestureDescription);
////            }
////        }, null);
//    }

    public GestureDescription gestureDescription(int startTimeMs, int durationMs, int x, int y) {
        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x,y);

        GestureDescription.StrokeDescription strokeDescription = new GestureDescription.StrokeDescription(path, startTimeMs, durationMs);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();

        gestureBuilder.addStroke(strokeDescription);

        return gestureBuilder.build();
    }

    public GestureDescription createGestureDescription(GestureDescription.StrokeDescription... strokes) {
        GestureDescription.Builder builder = new GestureDescription.Builder();
        for (GestureDescription.StrokeDescription stroke : strokes) {
            builder.addStroke(stroke);
        }
        return builder.build();
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
            boolean isCalled = dispatchGesture(gestureDescription(0, 10, mX, mY), null, null);
            Log.d("GestureDispatch", "Gesture dispatched: " + isCalled);
            mHandler.postDelayed(this, 1000); // Запуск следующего касания через 1 секунду
        }
    }
}
