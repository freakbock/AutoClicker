package com.example.autoclicker.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Presset {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
}
