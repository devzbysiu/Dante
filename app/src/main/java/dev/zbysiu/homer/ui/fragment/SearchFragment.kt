package dev.zbysiu.homer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dev.zbysiu.homer.R
import dev.zbysiu.homer.core.book.BookIds
import dev.zbysiu.homer.core.book.BookSearchItem
import dev.zbysiu.homer.core.data.BookRepository
import dev.zbysiu.homer.core.image.ImageLoader
import dev.zbysiu.homer.databinding.FragmentSearchBinding
import dev.zbysiu.homer.injection.AppComponent
import dev.zbysiu.homer.injection.ViewModelFactory
import dev.zbysiu.homer.ui.activity.DetailActivity
import dev.zbysiu.homer.ui.activity.SearchActivity
import dev.zbysiu.homer.ui.adapter.BookSearchSuggestionAdapter
import dev.zbysiu.homer.ui.viewmodel.SearchViewModel
import dev.zbysiu.homer.util.hideKeyboard
import dev.zbysiu.homer.util.viewModelOfActivity
import at.shockbytes.util.adapter.BaseAdapter
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    03.02.2018
 */
class SearchFragment : BaseFragment<FragmentSearchBinding>(),
    BaseAdapter.OnItemClickListener<BookSearchItem> {

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(inflater, root, attachToRoot)
    }

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var vmFactory: ViewModelFactory

    @Inject
    lateinit var bookRepository: BookRepository

    private lateinit var viewModel: SearchViewModel

    private val addClickedListener: ((BookSearchItem) -> Unit) = {
        activity?.hideKeyboard()
        vb.fragmentSearchSearchview.setSearchFocused(false)
        viewModel.requestBookDownload(it)
    }

    private val deleteClickedListener: ((BookSearchItem) -> Unit) = {
        bookRepository.delete(it.bookId).blockingAwait()
    }

    private lateinit var rvAdapter: BookSearchSuggestionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOfActivity(activity as SearchActivity, vmFactory)
    }

    override fun setupViews() {

        requireContext().let { ctx ->
            rvAdapter = BookSearchSuggestionAdapter(
                ctx,
                imageLoader,
                addClickedListener,
                deleteClickedListener,
                onItemClickListener = this
            )
            vb.fragmentSearchRv.layoutManager = LinearLayoutManager(context)
            vb.fragmentSearchRv.adapter = rvAdapter
            val dividerItemDecoration = DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL)
            dividerItemDecoration.setDrawable(
                ContextCompat.getDrawable(
                    ctx,
                    R.drawable.recycler_divider
                )!!
            )
            vb.fragmentSearchRv.addItemDecoration(dividerItemDecoration)
        }


        vb.fragmentSearchEmptyView.visibility = View.GONE

        vb.fragmentSearchSearchview.apply {
            homeActionClickListener = {
                activity?.supportFinishAfterTransition()
            }
            queryListener = { newQuery ->
                if (newQuery.toString() == "") {
                    rvAdapter.clear()
                    viewModel.requestInitialState()
                } else {
                    viewModel.showBooks(newQuery, keepLocal = true)
                }
            }
            setSearchFocused(true)
        }

        vb.fragmentSearchBtnSearchOnline.setOnClickListener {
            activity?.hideKeyboard()
            viewModel.showBooks(vb.fragmentSearchSearchview.currentQuery, keepLocal = false)
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {

        viewModel.getSearchState().observe(this) {
            when (it) {
                is SearchViewModel.SearchState.LoadingState -> {
                    vb.fragmentSearchSearchview.showProgress(true)
                    vb.fragmentSearchBtnSearchOnline.isEnabled = false
                }

                is SearchViewModel.SearchState.EmptyState -> {
                    vb.fragmentSearchSearchview.showProgress(false)
                    rvAdapter.clear()
                    vb.fragmentSearchEmptyView.visibility = View.VISIBLE
                    vb.fragmentSearchBtnSearchOnline.isEnabled = true
                }

                is SearchViewModel.SearchState.SuccessState -> {
                    vb.fragmentSearchSearchview.showProgress(false)
                    rvAdapter.data = it.items.toMutableList()
                    vb.fragmentSearchRv.scrollToPosition(0)
                    vb.fragmentSearchEmptyView.visibility = View.GONE
                    vb.fragmentSearchBtnSearchOnline.isEnabled = true
                }

                is SearchViewModel.SearchState.ErrorState -> {
                    showToast(message4SearchException(it.throwable))
                    vb.fragmentSearchSearchview.apply {
                        clearQuery()
                        showProgress(false)
                    }
                    vb.fragmentSearchEmptyView.visibility = View.GONE
                    vb.fragmentSearchBtnSearchOnline.isEnabled = true
                }

                is SearchViewModel.SearchState.InitialState -> {
                    vb.fragmentSearchSearchview.showProgress(false)
                    vb.fragmentSearchEmptyView.visibility = View.GONE
                    vb.fragmentSearchBtnSearchOnline.isEnabled = false
                }
            }
        }
    }

    override fun unbindViewModel() = Unit

    override fun onItemClick(content: BookSearchItem, position: Int, v: View) {
        activity?.hideKeyboard()
        if (BookIds.isValid(content.bookId)) {
            startActivity(DetailActivity.newIntent(requireContext(), content.bookId, content.title))
        }
    }

    private fun message4SearchException(t: Throwable): String {
        return when (t) {
            is UnknownHostException -> getString(R.string.no_internet_connection)
            else -> getString(R.string.search_invalid_query)
        }
    }

    companion object {

        fun newInstance(): SearchFragment {
            return SearchFragment()
        }
    }
}