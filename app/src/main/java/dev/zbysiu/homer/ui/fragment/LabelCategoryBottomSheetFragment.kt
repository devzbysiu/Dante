package dev.zbysiu.homer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dev.zbysiu.homer.R
import dev.zbysiu.homer.core.book.BookEntity
import dev.zbysiu.homer.core.book.BookLabel
import dev.zbysiu.homer.core.image.ImageLoader
import dev.zbysiu.homer.databinding.FragmentLabelCategoryBottomSheetBinding
import dev.zbysiu.homer.injection.AppComponent
import dev.zbysiu.homer.navigation.ActivityNavigator
import dev.zbysiu.homer.navigation.Destination
import dev.zbysiu.homer.ui.adapter.SimpleBookAdapter
import dev.zbysiu.homer.ui.viewmodel.LabelCategoryViewModel
import dev.zbysiu.homer.util.arguments.argument
import dev.zbysiu.homer.util.viewModelOf
import at.shockbytes.util.adapter.BaseAdapter
import javax.inject.Inject

class LabelCategoryBottomSheetFragment : BaseBottomSheetFragment<FragmentLabelCategoryBottomSheetBinding>() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var imageLoader: ImageLoader

    private lateinit var viewModel: LabelCategoryViewModel

    private var label: BookLabel by argument()

    private val simpleBookAdapter: SimpleBookAdapter by lazy {
        SimpleBookAdapter(
            requireContext(),
            imageLoader,
            object : BaseAdapter.OnItemClickListener<BookEntity> {
                override fun onItemClick(content: BookEntity, position: Int, v: View) {
                    ActivityNavigator.navigateTo(
                        context,
                        Destination.BookDetail(
                            Destination.BookDetail.BookDetailInfo(content.id, content.title)
                        ),
                        ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity()).toBundle()
                    )
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOf(vmFactory)
    }

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentLabelCategoryBottomSheetBinding {
        return FragmentLabelCategoryBottomSheetBinding.inflate(inflater, root, attachToRoot)
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.requestBooksWithLabel(label)
        viewModel.getBooks().observe(this, Observer(::showBooks))
    }

    private fun showBooks(books: List<BookEntity>) {
        simpleBookAdapter.updateData(books)
        vb.tvLabelCategoryDescription.text = resources.getQuantityString(R.plurals.books, books.size, books.size)
    }

    override fun unbindViewModel() = Unit

    override fun setupViews() {
        vb.rvLabelCategory.adapter = simpleBookAdapter
        vb.tvLabelCategoryHeader.apply {
            text = label.title
            setTextColor(label.labelHexColor.asColorInt())
        }
    }

    companion object {

        fun newInstance(label: BookLabel): LabelCategoryBottomSheetFragment {
            return LabelCategoryBottomSheetFragment().apply {
                this.label = label
            }
        }
    }
}