package dev.zbysiu.homer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dev.zbysiu.homer.R
import dev.zbysiu.homer.databinding.FragmentOnlineStorageBinding
import dev.zbysiu.homer.injection.AppComponent
import dev.zbysiu.homer.ui.viewmodel.OnlineStorageViewModel
import dev.zbysiu.homer.util.viewModelOf
import javax.inject.Inject

class OnlineStorageFragment : BaseFragment<FragmentOnlineStorageBinding>() {

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentOnlineStorageBinding {
        return FragmentOnlineStorageBinding.inflate(inflater, root, attachToRoot)
    }

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: OnlineStorageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOf(vmFactory)
    }

    override fun setupViews() {

        vb.btnOnlineStorageInterested.setOnClickListener {
            viewModel.userIsInterested()
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {

        viewModel.requestButtonState()
        viewModel.getButtonState().observe(this, Observer(::updateInterestedButton))
    }

    private fun updateInterestedButton(buttonState: OnlineStorageViewModel.InterestedButtonState) {

        when (buttonState) {
            OnlineStorageViewModel.InterestedButtonState.DEFAULT -> {
                vb.btnOnlineStorageInterested.apply {
                    isEnabled = true
                    setText(R.string.online_storage_interested)
                }
            }
            OnlineStorageViewModel.InterestedButtonState.INTERESTED -> {
                vb.btnOnlineStorageInterested.apply {
                    isEnabled = false
                    setText(R.string.online_storage_already_interested)
                }
            }
        }
    }

    override fun unbindViewModel() {
    }

    companion object {

        fun newInstance(): OnlineStorageFragment {
            return OnlineStorageFragment()
        }
    }
}