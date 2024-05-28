package com.example.autoclicker.Entity;

import androidx.room.PrimaryKey;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(foreignKeys = @ForeignKey(entity = Presset.class,
        parentColumns = "id",
        childColumns = "presetId",
        onDelete = ForeignKey.CASCADE))
public class PresetAction {

    @PrimaryKey(autoGenerate = true)
    public int presetActionId;
    public int presetId;
    public float x;
    public float y;
    public float endX;
    public float endY;
    public long duration;
    public String type;
}

