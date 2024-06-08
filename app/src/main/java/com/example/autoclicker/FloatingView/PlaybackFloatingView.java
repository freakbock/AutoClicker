    package com.example.autoclicker.FloatingView;

    import android.app.Service;
    import android.content.Intent;
    import android.graphics.PixelFormat;
    import android.os.IBinder;
    import android.util.DisplayMetrics;
    import android.util.Log;
    import android.view.Gravity;
    import android.view.LayoutInflater;
    import android.view.MotionEvent;
    import android.view.View;
    import android.view.WindowManager;
    import android.widget.FrameLayout;
    import android.widget.ImageView;

    import com.example.autoclicker.AccessibilityServices.PlaybackService;
    import com.example.autoclicker.MainActivity;
    import com.example.autoclicker.R;

    public class PlaybackFloatingView extends Service implements View.OnClickListener {

        private WindowManager mWindowManager;
        private View mFloatingView;
        private boolean isViewAttached = false;

        private int pressetId;

        boolean isRight = false;
        boolean isBottom = false;

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
            super.onCreate();

            Log.d("PlaybackFloatingView", "onCreate");
            // Подключаем XML разметку
            mFloatingView = LayoutInflater.from(this).inflate(R.layout.playback_floating_view, null);

            Log.d("PlaybackFloatingView", "Подключили XML разметку");

            // Устанавливаем параметры для плавающего окна
            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );

            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            mWindowManager.addView(mFloatingView, params);
            isViewAttached = true;

            Log.d("PlaybackFloatingView", "Добавлена view");

            mFloatingView.findViewById(R.id.playbackFloatingView).setOnTouchListener(new View.OnTouchListener() {
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
                            mWindowManager.updateViewLayout(mFloatingView, params);

                            return true;
                    }
                    return false;
                }
            });

            Log.d("PlaybackFloatingView", "onTouch подключен");

            // Настройка кнопок
            mFloatingView.findViewById(R.id.startPlayback).setOnClickListener(this);
            mFloatingView.findViewById(R.id.stopPlayback).setOnClickListener(this);
        }

        public int dpToPx(int dp) {
            float density = getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            if (intent != null) {
                pressetId = intent.getIntExtra("presetId", -1);
            }
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.d("PlaybackFloatingView", "onDestroy");
            if (isViewAttached && mFloatingView != null) {
                try {
                    mWindowManager.removeView(mFloatingView);
                    isViewAttached = false;
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(this, PlaybackService.class);

            if(v.getId() == R.id.startPlayback){
                intent.putExtra("action", "play");
                intent.putExtra("presetId", pressetId);
                startService(intent);

            }
            else if(v.getId() == R.id.stopPlayback){
                intent.putExtra("action", "stop");
                startService(intent);
                stopSelf();
                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
            }
        }
    }
