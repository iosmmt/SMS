package com.example.qjm;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface PickupInfoDao {
    @Query("SELECT * FROM pickup_info ORDER BY status ASC, timestamp DESC")
    List<PickupInfo> getAllPickupInfos();

    @Query("SELECT * FROM pickup_info WHERE status = :status ORDER BY timestamp DESC")
    List<PickupInfo> getPickupInfosByStatus(int status);

    @Query("SELECT * FROM pickup_info WHERE code LIKE :keyword OR company LIKE :keyword OR address LIKE :keyword OR station LIKE :keyword ORDER BY status ASC, timestamp DESC")
    List<PickupInfo> searchPickupInfos(String keyword);

    @Insert
    void insert(PickupInfo pickupInfo);

    @Update
    void update(PickupInfo pickupInfo);

    @Delete
    void delete(PickupInfo pickupInfo);
    
    @Query("SELECT * FROM pickup_info WHERE timestamp > :timestamp ORDER BY status ASC, timestamp DESC")
    List<PickupInfo> getPickupInfosAfterTimestamp(long timestamp);
    
    @Query("SELECT * FROM pickup_info WHERE id = :id")
    PickupInfo getPickupInfoById(int id);
}