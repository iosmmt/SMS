package com.example.qjm;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {PickupInfo.class, Rule.class}, version = 2, exportSchema = false)
public abstract class PickupInfoDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "pickup_info_db";
    private static PickupInfoDatabase instance;

    public abstract PickupInfoDao pickupInfoDao();
    public abstract RuleDao ruleDao();

    public static synchronized PickupInfoDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    PickupInfoDatabase.class,
                    DATABASE_NAME
            ).fallbackToDestructiveMigration() // 开发阶段使用，生产环境应实现具体的迁移策略
                    .build();
        }
        return instance;
    }
}