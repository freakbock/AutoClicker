package com.example.autoclicker.Entity;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class PresetWithActions {
    @Embedded public Presset preset;
    @Relation(
            parentColumn = "id",
            entityColumn = "presetId"
    )
    public List<PresetAction> actions;
}
