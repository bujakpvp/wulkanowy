package io.github.wulkanowy.ui.modules.message

import io.github.wulkanowy.ui.base.BasePresenter
import io.github.wulkanowy.ui.base.ErrorHandler
import io.github.wulkanowy.utils.SchedulersProvider
import io.reactivex.Completable
import timber.log.Timber
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject

class MessagePresenter @Inject constructor(
    errorHandler: ErrorHandler,
    private val schedulers: SchedulersProvider
) : BasePresenter<MessageView>(errorHandler) {

    override fun onAttachView(view: MessageView) {
        super.onAttachView(view)
        Timber.i("Message view is attached")
        disposable.add(Completable.timer(150, MILLISECONDS, schedulers.mainThread)
            .subscribe {
                view.initView()
                loadData()
            })
    }

    fun onPageSelected(index: Int) {
        loadChild(index)
    }

    private fun loadData() {
        view?.run { loadChild(currentPageIndex) }
    }

    private fun loadChild(index: Int, forceRefresh: Boolean = false) {
        Timber.i("Load message child view index: $index")
        view?.notifyChildLoadData(index, forceRefresh)
    }

    fun onChildViewLoaded() {
        view?.apply {
            showContent(true)
            showProgress(false)
        }
    }
}
