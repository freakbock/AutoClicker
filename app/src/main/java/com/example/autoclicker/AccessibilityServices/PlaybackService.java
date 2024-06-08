package com.example.autoclicker.AccessibilityServices;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.room.Room;

import com.example.autoclicker.AppDatabase;
import com.example.autoclicker.Entity.PresetAction;
import com.example.autoclicker.MainActivity;

import java.util.List;

public class PlaybackService extends AccessibilityService {

    private boolean isPlaying = false;
    private Handler mHandler;
    private List<PresetAction> actions;
    private int currentActionIndex;
    private AppDatabase database;
    private int presetId;

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread handlerThread = new HandlerThread("playback-handler");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
        database = Room.databaseBuilder(
                        getApplicationContext(),
                        AppDatabase.class,
                        "database_autoclicker")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Override
    protected void onServiceConnected() {
        Log.d("PlaybackService", "Service connected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_TOUCH_INTERACTION_START | AccessibilityEvent.TYPE_TOUCH_INTERACTION_END;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        setServiceInfo(info);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getStringExtra("action");
            if(action.equals("play")){
                presetId = intent.getIntExtra("presetId", -1);
                if (presetId != -1) {
                    isPlaying = true; // Установка флага, что воспроизведение началось
                    loadActionsFromDatabase(presetId);
                    Toast.makeText(getBaseContext(), "Playback Started", Toast.LENGTH_SHORT).show();
                }
            }
            else if(action.equals("stop")){
                stopPlayback();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void performAction(PresetAction action) {
        if (!isPlaying) {
            return;
        }

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path path = new Path();

        if ("click".equals(action.type)) {
            path.moveTo(action.x, action.y);
            GestureDescription.StrokeDescription stroke =
                    new GestureDescription.StrokeDescription(
                            path,
                            0,
                            100);
            gestureBuilder.addStroke(stroke);
            dispatchGesture(gestureBuilder.build(), null, null);
        }
        else if("swipe".equals(action.type)){
            path.moveTo(action.x, action.y);
            path.lineTo(action.endX, action.endY);
            GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(
                    path, 0, 500);

            gestureBuilder.addStroke(stroke);
        }

        dispatchGesture(gestureBuilder.build(), null, null);
    }

    private void stopPlayback() {
        mHandler.removeCallbacksAndMessages(null); // Остановка воспроизведения
        isPlaying = false;
    }

    private void loadActionsFromDatabase(int presetId) {
        new Thread(() -> {
            actions = database.actionDao().getActionsForPreset(presetId);
            currentActionIndex = 0;
            mHandler.post(playNextAction);
        }).start();
    }

    private final Runnable playNextAction = new Runnable() {
        @Override
        public void run() {
            if (currentActionIndex < actions.size()) {
                PresetAction action = actions.get(currentActionIndex);
                performAction(action);
                currentActionIndex++;
                mHandler.postDelayed(this, 1000);
            } else {
                Toast.makeText(getBaseContext(), "Playback Finished", Toast.LENGTH_SHORT).show();
                isPlaying = false; // Устанавливаем флаг в false после завершения воспроизведения
            }
        }
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }
    @Override
    public void onInterrupt() {

    }
}
