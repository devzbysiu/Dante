package dev.zbysiu.homer.ui.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import dev.zbysiu.homer.R
import dev.zbysiu.homer.core.image.ImageLoader
import dev.zbysiu.homer.databinding.FragmentTimelineBinding
import dev.zbysiu.homer.injection.AppComponent
import dev.zbysiu.homer.navigation.ActivityNavigator
import dev.zbysiu.homer.navigation.Destination.BookDetail
import dev.zbysiu.homer.navigation.Destination.BookDetail.BookDetailInfo
import dev.zbysiu.homer.timeline.TimeLineItem
import dev.zbysiu.homer.ui.adapter.timeline.TimeLineAdapter
import dev.zbysiu.homer.ui.viewmodel.TimelineViewModel
import dev.zbysiu.homer.util.getStringList
import dev.zbysiu.homer.util.setVisible
import dev.zbysiu.homer.util.viewModelOfActivity
import at.shockbytes.util.AppUtils
import at.shockbytes.util.adapter.BaseAdapter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import javax.inject.Inject

class TimeLineFragment : BaseFragment<FragmentTimelineBinding>() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var imageLoader: ImageLoader

    private lateinit var viewModel: TimelineViewModel

    private val timeLineAdapter: TimeLineAdapter by lazy {
        TimeLineAdapter(
            requireContext(),
            imageLoader,
            onItemClickListener = object : BaseAdapter.OnItemClickListener<TimeLineItem> {
                override fun onItemClick(content: TimeLineItem, position: Int, v: View) {
                    if (content is TimeLineItem.BookTimeLineItem) {
                        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        ActivityNavigator.navigateTo(
                            context,
                            BookDetail(BookDetailInfo(content.bookId, content.title)))
                    }
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = viewModelOfActivity(requireActivity(), vmFactory)
        viewModel.requestTimeline()
    }

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentTimelineBinding {
        return FragmentTimelineBinding.inflate(inflater, root, attachToRoot)
    }

    override fun setupViews() {
        vb.rvTimeline.apply {
            adapter = timeLineAdapter

            addItemDecoration(object : RecyclerView.ItemDecoration() {

                private val px = AppUtils.convertDpInPixel(16, requireContext())

                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

                    val position = parent.getChildAdapterPosition(view)
                    val count = parent.adapter?.itemCount?.dec()

                    when (position) {
                        0 -> outRect.top = px
                        count -> outRect.bottom = px
                    }
                }
            })
        }
        setupToolbar()
    }

    private fun setupToolbar() {

        with(vb.toolbarTimeline) {
            danteToolbarTitle.setText(R.string.label_timeline)
            danteToolbarBack.apply {
                setVisible(true)
                setOnClickListener {
                    activity?.onBackPressed()
                }
            }
            danteToolbarPrimaryAction.apply {
                setVisible(true)
                setImageResource(R.drawable.ic_timeline_sort)
                setOnClickListener {
                    showTimeLineDisplayPicker()
                }
            }
        }
    }

    private fun showTimeLineDisplayPicker() {
        MaterialDialog(requireContext())
                .title(R.string.dialogfragment_sort_by)
                .message(R.string.timeline_sort_explanation)
                .listItemsSingleChoice(
                        items = getStringList(R.array.sort_timeline),
                        initialSelection = viewModel.selectedTimeLineSortStrategyIndex
                ) { _, index, _ ->
                    viewModel.updateSortStrategy(index)
                }
                .icon(R.drawable.ic_timeline_sort)
                .cornerRadius(AppUtils.convertDpInPixel(6, requireContext()).toFloat())
                .cancelOnTouchOutside(true)
                .positiveButton(R.string.apply) {
                    it.dismiss()
                }
                .show()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.getTimeLineState().observe(this, Observer(::handleTimeLineState))
    }

    private fun handleTimeLineState(state: TimelineViewModel.TimeLineState) {
        when (state) {
            TimelineViewModel.TimeLineState.Loading -> handleLoadingState()
            TimelineViewModel.TimeLineState.Error -> handleErrorState()
            TimelineViewModel.TimeLineState.Empty -> handleEmptyState()
            is TimelineViewModel.TimeLineState.Success -> handleSuccessState(state.content)
        }
    }

    private fun handleLoadingState() {
        vb.rvTimeline.setVisible(false)
        vb.layoutTimelineError.setVisible(false)
        vb.layoutTimelineEmpty.setVisible(false)
        vb.pbTimelineLoading.setVisible(true)
    }

    private fun handleErrorState() {
        vb.rvTimeline.setVisible(false)
        vb.layoutTimelineEmpty.setVisible(false)
        vb.pbTimelineLoading.setVisible(false)
        vb.layoutTimelineError.setVisible(true)
    }

    private fun handleEmptyState() {
        vb.rvTimeline.setVisible(false)
        vb.layoutTimelineError.setVisible(false)
        vb.layoutTimelineEmpty.setVisible(true)
        vb.pbTimelineLoading.setVisible(false)
    }

    private fun handleSuccessState(content: List<TimeLineItem>) {
        vb.layoutTimelineError.setVisible(false)
        vb.layoutTimelineEmpty.setVisible(false)
        vb.pbTimelineLoading.setVisible(false)

        vb.rvTimeline.setVisible(true)

        timeLineAdapter.data.apply {
            clear()
            addAll(content)
        }
        timeLineAdapter.notifyDataSetChanged()
    }

    override fun unbindViewModel() {
        viewModel.getTimeLineState().removeObservers(this)
    }

    companion object {

        fun newInstance() = TimeLineFragment()
    }
}