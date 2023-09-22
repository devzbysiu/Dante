package dev.zbysiu.homer.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.zbysiu.homer.core.data.BookRepository
import dev.zbysiu.homer.timeline.TimeLineBuilder
import dev.zbysiu.homer.timeline.TimeLineItem
import dev.zbysiu.homer.util.ExceptionHandlers
import dev.zbysiu.homer.util.addTo
import dev.zbysiu.homer.util.settings.DanteSettings
import dev.zbysiu.homer.util.sort.TimeLineSortStrategy
import javax.inject.Inject

class TimelineViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val danteSettings: DanteSettings
) : BaseViewModel() {

    sealed class TimeLineState {

        object Loading : TimeLineState()
        object Error : TimeLineState()
        object Empty : TimeLineState()
        data class Success(val content: List<TimeLineItem>) : TimeLineState()
    }

    val selectedTimeLineSortStrategyIndex: Int
        get() = danteSettings.timeLineSortStrategy.ordinal

    private val timeLineState = MutableLiveData<TimeLineState>()
    fun getTimeLineState(): LiveData<TimeLineState> = timeLineState

    fun requestTimeline() {
        bookRepository.bookObservable
            .doOnSubscribe { postState(TimeLineState.Loading) }
            .doOnError { postState(TimeLineState.Error) }
            .map { books ->
                TimeLineBuilder.buildTimeLineItems(books, danteSettings.timeLineSortStrategy)
            }
            .map(::mapItemsToState)
            .subscribe(::postState, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    private fun postState(state: TimeLineState) {
        timeLineState.postValue(state)
    }

    private fun mapItemsToState(items: List<TimeLineItem>): TimeLineState {
        return if (items.isNotEmpty()) {
            TimeLineState.Success(items)
        } else {
            TimeLineState.Empty
        }
    }

    fun updateSortStrategy(index: Int) {
        if (selectedTimeLineSortStrategyIndex != index) {
            danteSettings.timeLineSortStrategy = TimeLineSortStrategy.ofOrdinal(index)

            requestTimeline()
        }
    }
}