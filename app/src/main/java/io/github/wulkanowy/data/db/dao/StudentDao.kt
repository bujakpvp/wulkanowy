package io.github.wulkanowy.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.FAIL
import androidx.room.Query
import io.github.wulkanowy.data.db.entities.Student
import io.reactivex.Maybe
import javax.inject.Singleton

@Singleton
@Dao
interface StudentDao {

    @Insert(onConflict = FAIL)
    fun insert(student: Student): Long

    @Delete
    fun delete(student: Student)

    @Query("SELECT * FROM Students WHERE is_current = 1")
    fun loadCurrent(): Maybe<Student>

    @Query("SELECT * FROM Students")
    fun loadAll(): Maybe<List<Student>>

    @Query("UPDATE Students SET is_current = 1 WHERE student_id = :studentId")
    fun updateCurrent(studentId: Int)

    @Query("UPDATE Students SET is_current = 0")
    fun resetCurrent()
}
