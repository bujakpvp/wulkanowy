package io.github.wulkanowy.data.repositories.remote

import io.github.wulkanowy.api.Api
import io.github.wulkanowy.data.db.entities.Attendance
import io.github.wulkanowy.data.db.entities.Semester
import io.github.wulkanowy.utils.toLocalDate
import io.reactivex.Single
import org.threeten.bp.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRemote @Inject constructor(private val api: Api) {

    fun getAttendance(semester: Semester, startDate: LocalDate, endDate: LocalDate): Single<List<Attendance>> {
        return Single.just(api.apply { diaryId = semester.diaryId })
            .flatMap { it.getAttendance(startDate, endDate) }.map { attendance ->
                attendance.map {
                    Attendance(
                        studentId = semester.studentId,
                        diaryId = semester.diaryId,
                        date = it.date.toLocalDate(),
                        number = it.number,
                        subject = it.subject,
                        name = it.name,
                        presence = it.presence,
                        absence = it.absence,
                        exemption = it.exemption,
                        lateness = it.lateness,
                        excused = it.excused,
                        deleted = it.deleted
                    )
                }
            }
    }
}
