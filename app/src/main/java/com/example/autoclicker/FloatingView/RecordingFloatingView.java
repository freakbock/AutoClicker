// RecordingFloatingView.java

package com.example.autoclicker.FloatingView;

import android.app.Service;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.room.Room;

import com.example.autoclicker.AppDatabase;
import com.example.autoclicker.Draw.OverlayView;
import com.example.autoclicker.Entity.PresetAction;
import com.example.autoclicker.Entity.Presset;
import com.example.autoclicker.MainActivity;
import com.example.autoclicker.R;

import java.util.Date;

public class RecordingFloatingView extends Service implements View.OnClickListener {
    private WindowManager mWindowManager;
    private View myFloatingView;
    private boolean isViewAttached = false;

    PresetAction currentAction;
    AppDatabase database;
    int presetId = -1;

    private Paint clickPaint;
    private Paint swipePaint;
    private Path swipePath;

    private OverlayView overlayView;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static final int SWIPE_THRESHOLD = 100; // Пороговое значение для расстояния свайпа
    private static final long CLICK_THRESHOLD = 200; // Пороговое значение для времени клика (в миллисекундах)

    private long startTime;
    private boolean isSwipe;



    @Override
    public void onCreate() {
        super.onCreate();

        clickPaint = new Paint();
        clickPaint.setColor(0xFFFF0000);
        clickPaint.setStyle(Paint.Style.FILL);

        swipePaint = new Paint();
        swipePaint.setColor(0xFF0000FF);
        swipePaint.setStyle(Paint.Style.STROKE);
        swipePaint.setStrokeWidth(5);

        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database_autoclicker").fallbackToDestructiveMigration().allowMainThreadQueries().build();
        presetId = createPreset();

        // Подключаем XML разметку
        myFloatingView = LayoutInflater.from(this).inflate(R.layout.recording_floating_view, null);
        myFloatingView.findViewById(R.id.finishRecording).setOnClickListener(this);


        overlayView = new OverlayView(this);
        // Устанавливаем параметры для плавающего окна
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(overlayView, params);
        mWindowManager.addView(myFloatingView, params);
        isViewAttached = true;

        myFloatingView.findViewById(R.id.recoringFloatingView).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private boolean isSwipeRegistered = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("RecordingFloatingView", "ACTION_DOWN detected");
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        swipePath = new Path();
                        swipePath.moveTo(initialTouchX, initialTouchY);
                        isSwipeRegistered = false; // Сброс флага при начале нового касания
                        return true;
                    case MotionEvent.ACTION_UP:
                        Log.d("RecordingFloatingView", "ACTION_UP detected");
                        if (isSwipeRegistered) {
                            saveSwipe("swipe", initialTouchX, initialTouchY, event.getRawX(), event.getRawY());
                        } else {
                            showClick(initialTouchX, initialTouchY);
                            saveClick("click", initialTouchX, initialTouchY);
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        Log.d("RecordingFloatingView", "ACTION_MOVE detected");
                        float endX = event.getRawX();
                        float endY = event.getRawY();
                        swipePath.lineTo(endX, endY);
                        showSwipe(swipePath);
                        if (!isSwipeRegistered && (Math.abs(endX - initialTouchX) > SWIPE_THRESHOLD || Math.abs(endY - initialTouchY) > SWIPE_THRESHOLD)) {
                            isSwipeRegistered = true;
                        }
                        return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isViewAttached && myFloatingView != null) {
            try {
                mWindowManager.removeView(myFloatingView);
                mWindowManager.removeView(overlayView);
                isViewAttached = false;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.finishRecording){
            stopSelf();
            Intent appMain = new Intent(getApplicationContext(), MainActivity.class);
            appMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplication().startActivity(appMain);
        }
    }

    private int createPreset() {
        Log.d("RecordingFloatingView", "Пресет создан");
        String name = "Новый пресет | " + new Date();
        Presset presset = new Presset(0, name);
        database.actionDao().insertPreset(presset);
        return database.actionDao().getPresetIdFromName(name);
    }

    private void saveClick(String type, float x, float y) {
        if(presetId != -1){
            currentAction = new PresetAction(
                    0,
                    presetId,
                    x,
                    y,
                    -1,
                    -1,
                    1000,
                    type
            );
            database.actionDao().insertAction(currentAction);
            Log.d("RecordingFloatingView", "Новый клик x="+x + ", y="+y);
        }
    }

    private void saveSwipe(String type, float x, float y, float x2, float y2){
        if(presetId !=-1){
            currentAction = new PresetAction(
                    0,
                    presetId,
                    x,
                    y,
                    x2,
                    y2,
                    500,
                    type
            );
            database.actionDao().insertAction(currentAction);
            Log.d("RecordingFloatingView", "saveSwipe: Новый свайп x="+x + " y="+y);
        }
    }

    private void showClick(float x, float y) {
        overlayView.addClick(x, y);
        overlayView.postDelayed(overlayView::clear, 1000); // Очистка view через 1 секунду
    }

    private void showSwipe(Path path) {
        overlayView.setSwipePath(path);
        overlayView.postDelayed(overlayView::clear, 1000); // Очистка view через 1 секунду
    }

}