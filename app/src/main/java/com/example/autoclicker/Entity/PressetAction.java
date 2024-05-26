package com.example.autoclicker.Entity;

import android.arch.persistence.room.PrimaryKey;

import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(foreignKeys = @ForeignKey(entity = Presset.class,
        parentColumns = "id",
        childColumns = "presetId",
        onDelete = ForeignKey.CASCADE))
public class PressetAction {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int presetId;
    public float x;
    public float y;
    public long duration;
    public String type;
}

