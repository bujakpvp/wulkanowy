package io.github.wulkanowy.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate
import java.io.Serializable

@Entity(tableName = "Notes")
data class Note(

    @ColumnInfo(name = "semester_id")
    var semesterId: Int,

    @ColumnInfo(name = "student_id")
    var studentId: Int,

    var date: LocalDate,

    var teacher: String,

    var category: String,

    var content: String
) : Serializable {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "is_read")
    var isRead: Boolean = false

    @ColumnInfo(name = "is_notified")
    var isNotified: Boolean = true
}
