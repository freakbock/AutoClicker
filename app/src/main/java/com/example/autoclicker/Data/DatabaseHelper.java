package com.example.autoclicker.Data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.autoclicker.Entity.PresetAction;
import com.example.autoclicker.Entity.PresetWithActions;
import com.example.autoclicker.Entity.Presset;

public class DatabaseHelper {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private static final String TAG = "DatabaseHelper";
    private static final String URL = "jdbc:mysql://192.168.31.51:3309/AutoClicker";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Настройка соединения
    public static void connectAsync(DatabaseConnectionCallback callback) {
        executorService.submit(() -> {
            Connection conn = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
                callback.onSuccess(conn);
            } catch (Exception e) {
                Log.e(TAG, "Connection failed: " + e.getMessage());
                e.printStackTrace();
                callback.onError(e);
            }
        });
    }

    public interface DatabaseConnectionCallback {
        void onSuccess(Connection connection);
        void onError(Exception e);
    }

    public interface DatabaseOperationCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    private static void handleConnectionFailure(DatabaseOperationCallback<?> callback, Exception e) {
        // Обработка ошибки подключения
        new Handler(Looper.getMainLooper()).post(() -> {
            // Здесь можно показать уведомление пользователю
            Log.e(TAG, "Connection error: " + e.getMessage());
        });
        callback.onError(e);
    }

    // Пример запроса данных
    public static void getDataAsync(DatabaseOperationCallback<List<Presset>> callback) {
        connectAsync(new DatabaseConnectionCallback() {
            @Override
            public void onSuccess(Connection conn) {
                executorService.submit(() -> {
                    List<Presset> presets = new ArrayList<>();
                    try {
                        String query = "SELECT * FROM Presset";
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(query);

                        while (rs.next()) {
                            int id = rs.getInt("id");
                            String name = rs.getString("name");
                            Presset preset = new Presset(id, name);
                            presets.add(preset);
                        }

                        rs.close();
                        stmt.close();
                        conn.close();
                        callback.onSuccess(presets);
                    } catch (Exception e) {
                        Log.e(TAG, "Query failed: " + e.getMessage());
                        e.printStackTrace();
                        callback.onError(e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                handleConnectionFailure(callback, e);
            }
        });
    }

    public static void addPresetAsync(String name, DatabaseOperationCallback<Integer> callback) {
        connectAsync(new DatabaseConnectionCallback() {
            @Override
            public void onSuccess(Connection conn) {
                executorService.submit(() -> {
                    int presetId = -1;
                    try {
                        String query = "INSERT INTO Presset (name) VALUES (?)";
                        PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                        pstmt.setString(1, name);
                        pstmt.executeUpdate();

                        ResultSet rs = pstmt.getGeneratedKeys();
                        if (rs.next()) {
                            presetId = rs.getInt(1);
                        }

                        rs.close();
                        pstmt.close();
                        conn.close();
                        callback.onSuccess(presetId);
                    } catch (Exception e) {
                        Log.e(TAG, "Insert Preset failed: " + e.getMessage());
                        e.printStackTrace();
                        callback.onError(e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                handleConnectionFailure(callback, e);
            }
        });
    }

    public static void addPresetActionAsync(int presetId, float x, float y, float endX, float endY, long duration, String type, DatabaseOperationCallback<Void> callback) {
        connectAsync(new DatabaseConnectionCallback() {
            @Override
            public void onSuccess(Connection conn) {
                executorService.submit(() -> {
                    try {
                        String query = "INSERT INTO PresetAction (presetId, x, y, endX, endY, duration, type) VALUES (?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement pstmt = conn.prepareStatement(query);
                        pstmt.setInt(1, presetId);
                        pstmt.setFloat(2, x);
                        pstmt.setFloat(3, y);
                        pstmt.setFloat(4, endX);
                        pstmt.setFloat(5, endY);
                        pstmt.setLong(6, duration);
                        pstmt.setString(7, type);
                        pstmt.executeUpdate();

                        pstmt.close();
                        conn.close();
                        callback.onSuccess(null);
                    } catch (Exception e) {
                        Log.e(TAG, "Insert PresetAction failed: " + e.getMessage());
                        e.printStackTrace();
                        callback.onError(e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                handleConnectionFailure(callback, e);
            }
        });
    }

    public static void loadPresetWithActionsAsync(int presetId, DatabaseOperationCallback<PresetWithActions> callback) {
        connectAsync(new DatabaseConnectionCallback() {
            @Override
            public void onSuccess(Connection conn) {
                executorService.submit(() -> {
                    PresetWithActions preset = new PresetWithActions();
                    try {
                        String presetQuery = "SELECT * FROM Presset WHERE id = ?";
                        PreparedStatement presetStmt = conn.prepareStatement(presetQuery);
                        presetStmt.setInt(1, presetId);
                        ResultSet presetRs = presetStmt.executeQuery();
                        if (presetRs.next()) {
                            int id = presetRs.getInt("id");
                            String name = presetRs.getString("name");
                            preset.preset = new Presset(id, name);
                        }

                        if (preset != null) {
                            String actionsQuery = "SELECT * FROM PresetAction WHERE presetId = ?";
                            PreparedStatement actionsStmt = conn.prepareStatement(actionsQuery);
                            actionsStmt.setInt(1, presetId);
                            ResultSet actionsRs = actionsStmt.executeQuery();

                            while (actionsRs.next()) {
                                int actionId = actionsRs.getInt("presetActionId");
                                float x = actionsRs.getFloat("x");
                                float y = actionsRs.getFloat("y");
                                float endX = actionsRs.getFloat("endX");
                                float endY = actionsRs.getFloat("endY");
                                long duration = actionsRs.getLong("duration");
                                String type = actionsRs.getString("type");

                                PresetAction action = new PresetAction(actionId, presetId, x, y, endX, endY, duration, type);
                                preset.actions.add(action);
                            }

                            actionsRs.close();
                            actionsStmt.close();
                        }

                        presetRs.close();
                        presetStmt.close();
                        conn.close();
                        callback.onSuccess(preset);
                    } catch (Exception e) {
                        Log.e(TAG, "Load Preset with Actions failed: " + e.getMessage());
                        e.printStackTrace();
                        callback.onError(e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                handleConnectionFailure(callback, e);
            }
        });
    }
}
