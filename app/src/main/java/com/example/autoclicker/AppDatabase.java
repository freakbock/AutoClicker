package com.example.autoclicker;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.autoclicker.Dao.ActionDao;
import com.example.autoclicker.Entity.Presset;
import com.example.autoclicker.Entity.PressetAction;

@Database(entities = {Presset.class, PressetAction.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ActionDao actionDao();
}

