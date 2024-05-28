package com.example.autoclicker.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.autoclicker.Entity.PresetAction;
import com.example.autoclicker.Entity.PresetWithActions;
import com.example.autoclicker.Entity.Presset;

import java.util.List;

@Dao
public interface ActionDao {

    @Insert
    void insertAction(PresetAction action);

    @Query("SELECT * FROM PresetAction WHERE presetId = :presetId")
    List<PresetAction> getActionsForPreset(int presetId);

    @Insert
    void insertPreset(Presset preset);

    @Query("SELECT id FROM Presset WHERE name = :name")
    int getPresetIdFromName(String name);

    @Query("SELECT * FROM Presset")
    List<Presset> getAllPresets();

    @Transaction
    @Query("SELECT * FROM Presset WHERE id = :presetId")
    PresetWithActions getPresetWithActions(int presetId);

    @Query("DELETE FROM Presset")
    void clearPressets();

    @Query("DELETE FROM PresetAction")
    void clearPressetActions();

    @Query("DELETE FROM Presset WHERE id = :id")
    void deletePressetById(int id);

    @Query("DELETE FROM PresetAction WHERE presetId = :id")
    void deletePressetActionByPressetId(int id);

    @Query("UPDATE Presset SET name = :name WHERE id = :id")
    void updateNamePressetById(int id, String name);
}
