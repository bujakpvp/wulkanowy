package io.github.wulkanowy.ui.modules.exam

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import io.github.wulkanowy.data.ErrorHandler
import io.github.wulkanowy.data.db.entities.Exam
import io.github.wulkanowy.data.repositories.ExamRepository
import io.github.wulkanowy.data.repositories.SessionRepository
import io.github.wulkanowy.ui.base.BasePresenter
import io.github.wulkanowy.utils.SchedulersProvider
import io.github.wulkanowy.utils.friday
import io.github.wulkanowy.utils.isHolidays
import io.github.wulkanowy.utils.logEvent
import io.github.wulkanowy.utils.monday
import io.github.wulkanowy.utils.nextOrSameSchoolDay
import io.github.wulkanowy.utils.toFormattedString
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDate.now
import org.threeten.bp.LocalDate.ofEpochDay
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject

class ExamPresenter @Inject constructor(
    private val errorHandler: ErrorHandler,
    private val schedulers: SchedulersProvider,
    private val examRepository: ExamRepository,
    private val sessionRepository: SessionRepository
) : BasePresenter<ExamView>(errorHandler) {

    lateinit var currentDate: LocalDate
        private set

    fun onAttachView(view: ExamView, date: Long?) {
        super.onAttachView(view)
        view.initView()
        loadData(ofEpochDay(date ?: now().nextOrSameSchoolDay.toEpochDay()))
        reloadView()
    }

    fun onPreviousWeek() {
        loadData(currentDate.minusDays(7))
        reloadView()
        logEvent("Exam week changed", mapOf("button" to "prev", "date" to currentDate.toFormattedString()))
    }

    fun onNextWeek() {
        loadData(currentDate.plusDays(7))
        reloadView()
        logEvent("Exam week changed", mapOf("button" to "next", "date" to currentDate.toFormattedString()))
    }

    fun onSwipeRefresh() {
        loadData(currentDate, true)
    }

    fun onExamItemSelected(item: AbstractFlexibleItem<*>?) {
        if (item is ExamItem) view?.showExamDialog(item.exam)
    }

    fun onViewReselected() {
        loadData(now().nextOrSameSchoolDay)
        reloadView()
    }

    private fun loadData(date: LocalDate, forceRefresh: Boolean = false) {
        currentDate = date
        disposable.apply {
            clear()
            add(sessionRepository.getSemesters()
                .delay(200, MILLISECONDS)
                .map { it.single { semester -> semester.current } }
                .flatMap {
                    examRepository.getExams(it, currentDate.monday, currentDate.friday, forceRefresh)
                }.map { it.groupBy { exam -> exam.date }.toSortedMap() }
                .map { createExamItems(it) }
                .subscribeOn(schedulers.backgroundThread)
                .observeOn(schedulers.mainThread)
                .doFinally {
                    view?.run {
                        hideRefresh()
                        showProgress(false)
                    }
                }
                .subscribe({
                    view?.apply {
                        updateData(it)
                        showEmpty(it.isEmpty())
                        showContent(it.isNotEmpty())
                    }
                    logEvent("Exam load", mapOf("items" to it.size, "forceRefresh" to forceRefresh, "date" to currentDate.toFormattedString()))
                }) {
                    view?.run { showEmpty(isViewEmpty) }
                    errorHandler.proceed(it)
                })
        }
    }

    private fun createExamItems(items: Map<LocalDate, List<Exam>>): List<ExamItem> {
        return items.flatMap {
            ExamHeader(it.key).let { header ->
                it.value.reversed().map { item -> ExamItem(header, item) }
            }
        }
    }

    private fun reloadView() {
        view?.apply {
            showProgress(true)
            showContent(false)
            showEmpty(false)
            clearData()
            showPreButton(!currentDate.minusDays(7).isHolidays)
            showNextButton(!currentDate.plusDays(7).isHolidays)
            updateNavigationWeek("${currentDate.monday.toFormattedString("dd.MM")} - " +
                currentDate.friday.toFormattedString("dd.MM"))
        }
    }
}