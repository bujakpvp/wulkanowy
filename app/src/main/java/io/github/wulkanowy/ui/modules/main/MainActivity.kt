package io.github.wulkanowy.ui.modules.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation.TitleState.ALWAYS_SHOW
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.ncapdevi.fragnav.FragNavController
import com.ncapdevi.fragnav.FragNavController.Companion.HIDE
import io.github.wulkanowy.R
import io.github.wulkanowy.services.notification.GradeNotification
import io.github.wulkanowy.ui.base.BaseActivity
import io.github.wulkanowy.ui.modules.account.AccountDialog
import io.github.wulkanowy.ui.modules.attendance.AttendanceFragment
import io.github.wulkanowy.ui.modules.exam.ExamFragment
import io.github.wulkanowy.ui.modules.grade.GradeFragment
import io.github.wulkanowy.ui.modules.more.MoreFragment
import io.github.wulkanowy.ui.modules.timetable.TimetableFragment
import io.github.wulkanowy.utils.getThemeAttrColor
import io.github.wulkanowy.utils.safelyPopFragment
import io.github.wulkanowy.utils.setOnViewChangeListener
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseActivity(), MainView {

    @Inject
    lateinit var presenter: MainPresenter

    @Inject
    lateinit var navController: FragNavController

    companion object {
        const val EXTRA_START_MENU_INDEX = "extraStartMenuIndex"

        fun getStartIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    override val isRootView: Boolean
        get() = navController.isRootFragment

    override val currentViewTitle: String?
        get() = (navController.currentFrag as? MainView.TitledView)?.titleStringId?.let { getString(it) }

    override val currentStackSize: Int?
        get() = navController.currentStack?.size

    override var startMenuIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(mainToolbar)
        messageContainer = mainFragmentContainer

        presenter.onAttachView(this, intent.getIntExtra(EXTRA_START_MENU_INDEX, -1))
        navController.initialize(startMenuIndex, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_menu_main, menu)
        return true
    }

    override fun onStart() {
        super.onStart()
        presenter.onViewStart()
    }

    override fun initView() {
        mainBottomNav.run {
            addItems(
                mutableListOf(
                    AHBottomNavigationItem(R.string.grade_title, R.drawable.ic_menu_main_grade_26dp, 0),
                    AHBottomNavigationItem(R.string.attendance_title, R.drawable.ic_menu_main_attendance_24dp, 0),
                    AHBottomNavigationItem(R.string.exam_title, R.drawable.ic_menu_main_exam_24dp, 0),
                    AHBottomNavigationItem(R.string.timetable_title, R.drawable.ic_menu_main_timetable_24dp, 0),
                    AHBottomNavigationItem(R.string.more_title, R.drawable.ic_menu_main_more_24dp, 0)
                )
            )
            accentColor = ContextCompat.getColor(context, R.color.colorPrimary)
            inactiveColor = getThemeAttrColor(android.R.attr.textColorSecondary)
            defaultBackgroundColor = getThemeAttrColor(R.attr.bottomNavBackground)
            titleState = ALWAYS_SHOW
            currentItem = startMenuIndex
            isBehaviorTranslationEnabled = false
            setTitleTextSizeInSp(10f, 10f)
            setOnTabSelectedListener { position, wasSelected ->
                presenter.onTabSelected(position, wasSelected)
            }
        }

        navController.run {
            setOnViewChangeListener { presenter.onViewStart() }
            fragmentHideStrategy = HIDE
            rootFragments = listOf(
                GradeFragment.newInstance(),
                AttendanceFragment.newInstance(),
                ExamFragment.newInstance(),
                TimetableFragment.newInstance(),
                MoreFragment.newInstance()
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == R.id.mainMenuAccount) presenter.onAccountManagerSelected()
        else false
    }

    override fun onSupportNavigateUp(): Boolean {
        return presenter.onUpNavigate()
    }

    override fun switchMenuView(position: Int) {
        navController.switchTab(position)
    }

    override fun setViewTitle(title: String) {
        supportActionBar?.title = title
    }

    override fun showHomeArrow(show: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(show)
    }

    override fun showAccountPicker() {
        navController.showDialogFragment(AccountDialog.newInstance())
    }

    override fun notifyMenuViewReselected() {
        (navController.currentStack?.get(0) as? MainView.MainChildView)?.onFragmentReselected()
    }

    fun pushView(fragment: Fragment) {
        navController.pushFragment(fragment)
    }

    override fun popView() {
        navController.safelyPopFragment()
    }

    override fun onBackPressed() {
        presenter.onBackPressed { super.onBackPressed() }
    }

    override fun cancelNotifications() {
        GradeNotification(applicationContext).cancelAll()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        navController.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetachView()
    }
}