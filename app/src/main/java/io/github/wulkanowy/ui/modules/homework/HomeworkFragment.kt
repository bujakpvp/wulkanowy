package io.github.wulkanowy.ui.modules.homework

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import io.github.wulkanowy.R
import io.github.wulkanowy.data.db.entities.Homework
import io.github.wulkanowy.ui.base.session.BaseSessionFragment
import io.github.wulkanowy.ui.modules.main.MainActivity
import io.github.wulkanowy.ui.modules.main.MainView
import io.github.wulkanowy.utils.setOnItemClickListener
import kotlinx.android.synthetic.main.fragment_homework.*
import javax.inject.Inject

class HomeworkFragment : BaseSessionFragment(), HomeworkView, MainView.TitledView {

    @Inject
    lateinit var presenter: HomeworkPresenter

    @Inject
    lateinit var homeworkAdapter: FlexibleAdapter<AbstractFlexibleItem<*>>

    companion object {
        private const val SAVED_DATE_KEY = "CURRENT_DATE"

        fun newInstance() = HomeworkFragment()
    }

    override val titleStringId: Int
        get() = R.string.homework_title

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_homework, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        messageContainer = homeworkRecycler
        presenter.onAttachView(this, savedInstanceState?.getLong(HomeworkFragment.SAVED_DATE_KEY))
    }

    override fun initView() {
        homeworkAdapter.run {
            setOnItemClickListener { presenter.onHomeworkItemSelected(it) }
        }

        homeworkRecycler.run {
            layoutManager = SmoothScrollLinearLayoutManager(context)
            adapter = homeworkAdapter
        }
        homeworkSwipe.setOnRefreshListener { presenter.onSwipeRefresh() }
        homeworkPreviousButton.setOnClickListener { presenter.onPreviousDay() }
        homeworkNextButton.setOnClickListener { presenter.onNextDay() }
    }

    override fun updateData(data: List<HomeworkItem>) {
        homeworkAdapter.updateDataSet(data, true)
    }

    override fun clearData() {
        homeworkAdapter.clear()
    }

    override fun updateNavigationDay(date: String) {
        homeworkNavDate.text = date
    }

    override fun isViewEmpty() = homeworkAdapter.isEmpty

    override fun hideRefresh() {
        homeworkSwipe.isRefreshing = false
    }

    override fun showEmpty(show: Boolean) {
        homeworkEmpty.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showProgress(show: Boolean) {
        homeworkProgress.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showContent(show: Boolean) {
        homeworkRecycler.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showPreButton(show: Boolean) {
        homeworkPreviousButton.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    override fun showNextButton(show: Boolean) {
        homeworkNextButton.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    override fun showTimetableDialog(homework: Homework) {
        (activity as? MainActivity)?.showDialogFragment(HomeworkDialog.newInstance(homework))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(HomeworkFragment.SAVED_DATE_KEY, presenter.currentDate.toEpochDay())
    }

    override fun onDestroyView() {
        presenter.onDetachView()
        super.onDestroyView()
    }
}
