package com.example.autoclicker;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.autoclicker.AccessibilityServices.AutoClickService;
import com.example.autoclicker.AccessibilityServices.PlaybackService;
import com.example.autoclicker.Entity.PresetAction;
import com.example.autoclicker.Entity.PresetWithActions;
import com.example.autoclicker.Entity.Presset;
import com.example.autoclicker.FloatingView.FloatingView;
import com.example.autoclicker.FloatingView.PlaybackFloatingView;
import com.example.autoclicker.FloatingView.RecordingFloatingView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;

    private ActivityResultLauncher<Intent> accessibilitySettingsLauncher;
    private ActivityResultLauncher<Intent> overlayPermissionLauncher;

    public static AppDatabase database;

    LinearLayout pressets_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pressets_list = findViewById(R.id.pressets_list);

        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database_autoclicker").fallbackToDestructiveMigration().allowMainThreadQueries().build();

        LoadPressets();
//        for(Presset presset : database.actionDao().getAllPresets()){
//            Log.d("MainActivity", presset.name + "\n");
//            for(PresetAction presetAction : database.actionDao().getPresetWithActions(presset.id).actions){
//                Log.d(presset.name, presetAction.x + " | " + presetAction.y   + " | " + presetAction.endX + " | " + presetAction.endY   + " | " + presetAction.type  + " | " + presetAction.duration);
//            }
//        }

        // Register activity result launchers
        accessibilitySettingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Handle the result if needed
                });

        overlayPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Handle the result if needed
                    if (Settings.canDrawOverlays(this)) {
                        startFloatingService();
                    } else {
                        Toast.makeText(this, "Для этого вам необходимо разрешение окна системных предупреждений", Toast.LENGTH_SHORT).show();
                    }
                });

        if (!Settings.canDrawOverlays(this)) {
            askOverlayPermission();
        } else {
            checkAccessibilityPermission();
        }

        findViewById(R.id.startFloat).setOnClickListener(this);
        findViewById(R.id.createPresset).setOnClickListener(this);
        findViewById(R.id.loadPresset).setOnClickListener(this);
    }

        public int dpToPx(float dp) {
            // Получаем плотность экрана
            float density = getResources().getDisplayMetrics().density;
            // Конвертируем dp в пиксели
            return Math.round(dp * density);
        }


    // Реализация слушателя двойного клика
    public abstract static class DoubleClickListener implements View.OnClickListener {
        private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Временной интервал для двойного клика
        private long lastClickTime = 0;

        public abstract void onDoubleClick(View v);

        @Override
        public void onClick(View v) {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                onDoubleClick(v);
            }
            lastClickTime = clickTime;
        }
    }
    private void LoadPressets(){
        pressets_list.removeAllViews();

        for(Presset presset: database.actionDao().getAllPresets()){
            LinearLayout parent = new LinearLayout(this);
            LinearLayout.LayoutParams parentParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            parentParams.setMargins(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
            parent.setPadding(dpToPx(5),dpToPx(5),dpToPx(5),dpToPx(5));
            parent.setLayoutParams(parentParams);
            parent.setOrientation(LinearLayout.VERTICAL);

            TextView name = new TextView(this);
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            name.setTextSize(16f);
            name.setTextColor(getColor(R.color.black));
            name.setLayoutParams(nameParams);
            name.setText(presset.name);
            name.setOnClickListener(new DoubleClickListener() {
                @Override
                public void onDoubleClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Изменить текст");

                    // Установите поле для ввода текста в диалоге
                    final EditText input = new EditText(MainActivity.this);
                    builder.setView(input);

                    // Установите кнопки "ОК" и "Отмена"
                    builder.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String newText = input.getText().toString();
                            if(!newText.isEmpty()){
                                name.setText(newText);
                                database.actionDao().updateNamePressetById(presset.id, newText);
                            }
                        }
                    });
                    builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    // Покажите диалог
                    builder.show();
                }
            });

            LinearLayout childrenLayout = new LinearLayout(this);
            LinearLayout.LayoutParams childrenLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dpToPx(50)
            );
            childrenLayout.setLayoutParams(childrenLayoutParams);

            Button delete = new Button(this);
            LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                    dpToPx(100),
                    dpToPx(50)
            );
            delete.setLayoutParams(deleteParams);
            delete.setText("Удалить");
            delete.setTextSize(12f);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    database.actionDao().deletePressetById(presset.id);
                    database.actionDao().deletePressetActionByPressetId(presset.id);
                    parent.setVisibility(View.GONE);
                }
            });


            Button send = new Button(this);
            LinearLayout.LayoutParams sendParams = new LinearLayout.LayoutParams(
                    dpToPx(100),
                    dpToPx(50)
            );
            send.setLayoutParams(sendParams);
            send.setText("Выгрузить");
            send.setTextSize(12f);
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            Button play = new Button(this);
            LinearLayout.LayoutParams playParams = new LinearLayout.LayoutParams(
                    dpToPx(120),
                    dpToPx(50)
            );
            play.setLayoutParams(playParams);
            play.setText("Использовать");
            play.setTextSize(12f);
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, PlaybackFloatingView.class);
                    intent.putExtra("presetId", presset.id);
                    startService(intent);
                    finish();
                }
            });

            childrenLayout.addView(delete);
            childrenLayout.addView(send);
            childrenLayout.addView(play);

            parent.addView(name);
            parent.addView(childrenLayout);

            pressets_list.addView(parent);

        }

    }

    private void askOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        overlayPermissionLauncher.launch(intent);
    }

    private void checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            accessibilitySettingsLauncher.launch(intent);
            Toast.makeText(this, "Пожалуйста включите спец.возможности для корректной работы", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        String service = getPackageName() + "/" + AutoClickService.class.getCanonicalName();
        String enabledServices = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return enabledServices != null && enabledServices.contains(service);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.startFloat){
            if (Settings.canDrawOverlays(this)) {
                if (isAccessibilityServiceEnabled()) {

                    startFloatingService();

                    finish();
                } else {
                    checkAccessibilityPermission();
                }
            } else {
                askOverlayPermission();
            }
        }
        else if(v.getId() == R.id.createPresset){
            if (Settings.canDrawOverlays(this)) {
                if (isAccessibilityServiceEnabled()) {

                    startRecordingFloatingService();

                    finish();
                } else {
                    checkAccessibilityPermission();
                }
            } else {
                askOverlayPermission();
            }
        }
        else if(v.getId() == R.id.loadPresset){
            if (Settings.canDrawOverlays(this)) {
                if (isAccessibilityServiceEnabled()) {



                } else {
                    checkAccessibilityPermission();
                }
            } else {
                askOverlayPermission();
            }
        }
    }

    private void startFloatingService() {
        startService(new Intent(MainActivity.this, FloatingView.class));
    }
    private void startRecordingFloatingService(){
        startService(new Intent(MainActivity.this, RecordingFloatingView.class));
    }
}
