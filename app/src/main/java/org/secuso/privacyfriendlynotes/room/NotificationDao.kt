package org.secuso.privacyfriendlynotes.room

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface NotificationDao {
    @Insert(onConflict = REPLACE)
    fun insert(notification: Notification)

    @Update(onConflict = REPLACE)
    fun update(notification: Notification)

    @Delete
    fun delete(notification: Notification)

    @Query("SELECT * FROM notifications WHERE noteid=:thisNoteid ")
    suspend fun notificationFromNoteId(thisNoteid: Integer): Notification

    @get:Query("SELECT * FROM notifications")
    val allNotifications: LiveData<List<Notification>>
}