package com.example.autoclicker;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;

    private ActivityResultLauncher<Intent> accessibilitySettingsLauncher;
    private ActivityResultLauncher<Intent> overlayPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                        Toast.makeText(this, "You need System Alert Window Permission to do this", Toast.LENGTH_SHORT).show();
                    }
                });

        if (!Settings.canDrawOverlays(this)) {
            askOverlayPermission();
        } else {
            checkAccessibilityPermission();
        }

        findViewById(R.id.startFloat).setOnClickListener(this);
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
            Toast.makeText(this, "Please enable Accessibility Service for AutoClicker", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        String service = getPackageName() + "/" + AutoClickService.class.getCanonicalName();
        String enabledServices = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return enabledServices != null && enabledServices.contains(service);
    }

    @Override
    public void onClick(View v) {
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

    private void startFloatingService() {
        startService(new Intent(MainActivity.this, FloatingView.class));
    }
}
