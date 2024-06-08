package com.example.autoclicker.FloatingView;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.autoclicker.AccessibilityServices.AutoClickService;
import com.example.autoclicker.MainActivity;
import com.example.autoclicker.R;

public class FloatingView extends Service implements View.OnClickListener {
    private WindowManager mWindowManager;
    private View myFloatingView;
    private boolean isViewAttached = false;  // Флаг для отслеживания состояния прикрепления представления

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // получение разметки виджета из XML с помощью LayoutInflater
        myFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null);

        // установка параметров разметки
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        // получение WindowManager и добавление плавающего виджета
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(myFloatingView, params);
        isViewAttached = true;  // Устанавливаем флаг в true после добавления представления

        myFloatingView.findViewById(R.id.thisIsAnID).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(myFloatingView, params);
                        return true;
                }
                return false;
            }
        });

        Button startButton = (Button) myFloatingView.findViewById(R.id.start);
        startButton.setOnClickListener(this);
        Button stopButton = (Button) myFloatingView.findViewById(R.id.stop);
        stopButton.setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isViewAttached && myFloatingView != null) {
            try {
                mWindowManager.removeView(myFloatingView);
                isViewAttached = false;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), AutoClickService.class);
        int viewId = v.getId();

        if (viewId == R.id.start) {
            int[] location = new int[2];
            myFloatingView.getLocationOnScreen(location);
            intent.putExtra("action", "play");
            intent.putExtra("x", location[0] - 1);
            intent.putExtra("y", location[1] - 1);
        } else if (viewId == R.id.stop) {
            intent.putExtra("action", "stop");
            if (isViewAttached && myFloatingView != null) {
                try {
                    mWindowManager.removeView(myFloatingView);
                    isViewAttached = false;  // Устанавливаем флаг в false после удаления представления
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            Intent appMain = new Intent(getApplicationContext(), MainActivity.class);
            appMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplication().startActivity(appMain);
            stopSelf();
        }

        getApplication().startService(intent);
    }
}