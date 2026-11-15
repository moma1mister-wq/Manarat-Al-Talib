package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Users
    @Query("SELECT * FROM user_accounts WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccountEntity)

    @Query("DELETE FROM user_accounts")
    suspend fun clearAllUsers()

    // Schedule
    @Query("SELECT * FROM schedule_items ORDER BY startTime ASC")
    fun getAllScheduleItems(): Flow<List<ScheduleItemEntity>>

    @Query("SELECT * FROM schedule_items ORDER BY startTime ASC")
    suspend fun getAllScheduleItemsOnce(): List<ScheduleItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItem(item: ScheduleItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItems(items: List<ScheduleItemEntity>)

    @Update
    suspend fun updateScheduleItem(item: ScheduleItemEntity)

    @Delete
    suspend fun deleteScheduleItem(item: ScheduleItemEntity)

    @Query("DELETE FROM schedule_items")
    suspend fun clearAllScheduleItems()

    // Session Ratings
    @Query("SELECT * FROM session_ratings ORDER BY timestamp DESC")
    fun getAllRatings(): Flow<List<SessionRatingEntity>>

    @Query("SELECT * FROM session_ratings ORDER BY timestamp DESC")
    suspend fun getAllRatingsOnce(): List<SessionRatingEntity>

    @Query("SELECT * FROM session_ratings WHERE username = :username ORDER BY timestamp DESC")
    suspend fun getRatingsForUserOnce(username: String): List<SessionRatingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: SessionRatingEntity)

    @Delete
    suspend fun deleteRating(rating: SessionRatingEntity)

    @Query("DELETE FROM session_ratings WHERE username = :username")
    suspend fun clearRatingsForUser(username: String)

    @Query("DELETE FROM session_ratings")
    suspend fun clearAllRatings()

    // Catalog Items (Hisn Al-Muslim PDF Extraction + Student Prices List)
    @Query("SELECT * FROM catalog_items ORDER BY pdfPageNumber ASC, id ASC")
    fun getAllCatalogItems(): Flow<List<CatalogItemEntity>>

    @Query("SELECT * FROM catalog_items ORDER BY id ASC")
    suspend fun getAllCatalogItemsOnce(): List<CatalogItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatalogItem(item: CatalogItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatalogItems(items: List<CatalogItemEntity>)

    @Update
    suspend fun updateCatalogItem(item: CatalogItemEntity)

    @Delete
    suspend fun deleteCatalogItem(item: CatalogItemEntity)

    @Query("DELETE FROM catalog_items WHERE recordType = :type")
    suspend fun deleteCatalogItemsByType(type: String)

    // Adhkar Alarms
    @Query("SELECT * FROM adhkar_alarms ORDER BY id ASC")
    fun getAllAdhkarAlarms(): Flow<List<AdhkarAlarmEntity>>

    @Query("SELECT * FROM adhkar_alarms ORDER BY id ASC")
    suspend fun getAllAdhkarAlarmsOnce(): List<AdhkarAlarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdhkarAlarms(alarms: List<AdhkarAlarmEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdhkarAlarm(alarm: AdhkarAlarmEntity)

    @Update
    suspend fun updateAdhkarAlarm(alarm: AdhkarAlarmEntity)

    @Delete
    suspend fun deleteAdhkarAlarm(alarm: AdhkarAlarmEntity)
}
