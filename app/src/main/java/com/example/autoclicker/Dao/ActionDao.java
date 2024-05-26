package com.example.autoclicker.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.autoclicker.Entity.Presset;
import com.example.autoclicker.Entity.PressetAction;
import com.example.autoclicker.Entity.PressetWithActions;

import java.util.List;

public interface ActionDao {

    @Insert
    void insertAction(PressetAction action);

    @Query("SELECT * FROM PressetAction WHERE presetId = :presetId")
    List<PressetAction> getActionsForPreset(int presetId);

    @Insert
    void insertPreset(Presset preset);

    @Query("SELECT * FROM Presset")
    List<Presset> getAllPresets();

    @Transaction
    @Query("SELECT * FROM Presset WHERE id = :presetId")
    PressetWithActions getPresetWithActions(int presetId);

}
