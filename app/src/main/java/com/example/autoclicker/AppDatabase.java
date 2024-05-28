package com.example.autoclicker;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.autoclicker.Dao.ActionDao;
import com.example.autoclicker.Entity.Presset;
import com.example.autoclicker.Entity.PresetAction;

@Database(entities = {Presset.class, PresetAction.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ActionDao actionDao();
}

