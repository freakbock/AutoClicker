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


    public PresetAction(int presetActionId, int presetId, float x, float y, float endX, float endY, long duration, String type) {
        this.presetActionId = presetActionId;
        this.presetId = presetId;
        this.x = x;
        this.y = y;
        this.endX = endX;
        this.endY = endY;
        this.duration = duration;
        this.type = type;
    }

    public int getActionId() {
        return presetActionId;
    }

    public int getPresetId() {
        return presetId;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getEndX() {
        return endX;
    }

    public float getEndY() {
        return endY;
    }

    public long getDuration() {
        return duration;
    }

    public String getType() {
        return type;
    }
}

