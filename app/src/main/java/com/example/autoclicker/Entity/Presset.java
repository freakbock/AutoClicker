package com.example.autoclicker.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Presset {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public Presset(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
