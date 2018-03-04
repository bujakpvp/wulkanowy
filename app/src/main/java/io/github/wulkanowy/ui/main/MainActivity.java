package io.github.wulkanowy.ui.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.wulkanowy.R;
import io.github.wulkanowy.services.SyncJob;
import io.github.wulkanowy.ui.base.BaseActivity;
import io.github.wulkanowy.ui.main.dashboard.DashboardFragment;
import io.github.wulkanowy.ui.main.grades.GradesFragment;
import io.github.wulkanowy.ui.main.timetable.TimetableFragment;

public class MainActivity extends BaseActivity implements MainContract.View,
        AHBottomNavigation.OnTabSelectedListener, OnFragmentIsReadyListener {

    private int initTabPosition = 0;

    @BindView(R.id.main_activity_nav)
    AHBottomNavigation bottomNavigation;

    @BindView(R.id.main_activity_view_pager)
    AHBottomNavigationViewPager viewPager;

    @BindView(R.id.main_activity_progress_bar)
    View progressBar;

    @Inject
    MainPagerAdapter pagerAdapter;

    @Inject
    MainContract.Presenter presenter;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initTabPosition = getIntent().getIntExtra(SyncJob.EXTRA_INTENT_KEY, initTabPosition);

        getActivityComponent().inject(this);
        setButterKnife(ButterKnife.bind(this));

        presenter.onStart(this);

        initiationViewPager();
        initiationBottomNav();
    }

    @Override
    public void showProgressBar(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        viewPager.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        bottomNavigation.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void showActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }

    @Override
    public void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    public boolean onTabSelected(int position, boolean wasSelected) {
        presenter.onTabSelected(position, wasSelected);
        return true;
    }

    @Override
    public void setCurrentPage(int position) {
        viewPager.setCurrentItem(position, false);
    }

    @Override
    public void onFragmentIsReady() {
        presenter.onFragmentIsReady();
    }

    private void initiationBottomNav() {
        bottomNavigation.addItem(new AHBottomNavigationItem(
                getString(R.string.grades_text),
                getResources().getDrawable(R.drawable.ic_menu_grade_26dp)
        ));
        bottomNavigation.addItem(new AHBottomNavigationItem(
                getString(R.string.attendance_text),
                getResources().getDrawable(R.drawable.ic_menu_attendance_24dp)
        ));
        bottomNavigation.addItem(new AHBottomNavigationItem(
                getString(R.string.dashboard_text),
                getResources().getDrawable(R.drawable.ic_menu_dashboard_24dp)
        ));
        bottomNavigation.addItem(new AHBottomNavigationItem(
                getString(R.string.lessonplan_text),
                getResources().getDrawable(R.drawable.ic_menu_timetable_24dp)
        ));
        bottomNavigation.addItem(new AHBottomNavigationItem(
                getString(R.string.settings_text),
                getResources().getDrawable(R.drawable.ic_menu_other_24dp)
        ));

        bottomNavigation.setAccentColor(getResources().getColor(R.color.colorPrimary));
        bottomNavigation.setInactiveColor(Color.BLACK);
        bottomNavigation.setBackgroundColor(getResources().getColor(R.color.colorBackgroundBottomNav));
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        bottomNavigation.setOnTabSelectedListener(this);
        bottomNavigation.setCurrentItem(initTabPosition);
        bottomNavigation.setBehaviorTranslationEnabled(false);
    }

    private void initiationViewPager() {
        pagerAdapter.addFragment(new GradesFragment());
        pagerAdapter.addFragment(new DashboardFragment());
        pagerAdapter.addFragment(new DashboardFragment());
        pagerAdapter.addFragment(new TimetableFragment());
        pagerAdapter.addFragment(new DashboardFragment());

        viewPager.setPagingEnabled(false);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(4);
        viewPager.setCurrentItem(initTabPosition, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}