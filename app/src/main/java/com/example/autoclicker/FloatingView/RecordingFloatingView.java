// RecordingFloatingView.java

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

import androidx.room.Room;

import com.example.autoclicker.AppDatabase;
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


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Подключаем XML разметку
        myFloatingView = LayoutInflater.from(this).inflate(R.layout.recording_floating_view, null);

        // Устанавливаем параметры для плавающего окна
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(myFloatingView, params);
        isViewAttached = true;

        myFloatingView.findViewById(R.id.recoringFloatingView).setOnTouchListener(new View.OnTouchListener() {
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
                        // этот код позволяет перемещать виджет по экрану с помощью пальцев
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(myFloatingView, params);
                        return true;
                }
                return false;
            }
        });

        // Настройка кнопок
        myFloatingView.findViewById(R.id.click).setOnClickListener(this);
        myFloatingView.findViewById(R.id.finishRecording).setOnClickListener(this);

        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database_autoclicker").fallbackToDestructiveMigration().allowMainThreadQueries().build();
        presetId = createPreset();
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
        if(v.getId() == R.id.click){
            int[] location = new int[2];
            myFloatingView.getLocationOnScreen(location);
            saveClick("click", location[0] -1, location[1] -1);

        }
        else if(v.getId() == R.id.finishRecording){
            Intent appMain = new Intent(getApplicationContext(), MainActivity.class);
            appMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplication().startActivity(appMain);
            stopSelf();
        }
    }

    private int createPreset() {
        Log.d("RecordingFloatingView", "Пресет создан");
        String name = "Новый пресет | " + new Date();
        Presset presset = new Presset();
        presset.name = name;
        database.actionDao().insertPreset(presset);
        return database.actionDao().getPresetIdFromName(name);
    }

    private void saveClick(String type, float x, float y) {
        if(presetId != -1){
            currentAction = new PresetAction();
            currentAction.presetId = presetId;
            currentAction.x = x;
            currentAction.y = y;
            currentAction.endX = -1;
            currentAction.endY = -1;
            currentAction.duration = 1000;
            currentAction.type = type;
            database.actionDao().insertAction(currentAction);
            Log.d("RecordingFloatingView", "Новый клик x="+x + ", y="+y);
        }
    }

//    private void saveSwipe(String type, float x, float y, float endX, float endY) {
//        if(presetId != -1){
//            currentAction = new PresetAction();
//            currentAction.presetId = presetId;
//            currentAction.x = x;
//            currentAction.y = y;
//            currentAction.endX = endX;
//            currentAction.endY = endY;
//            currentAction.duration = System.currentTimeMillis();
//            currentAction.type = type;
//            database.actionDao().insertAction(currentAction);
//        }
//    }
}
