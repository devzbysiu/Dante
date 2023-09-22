package dev.zbysiu.homer.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import dev.zbysiu.homer.databinding.LoginLoadingFragmentBinding
import dev.zbysiu.homer.injection.AppComponent

class LoginLoadingFragment : BaseFragment<LoginLoadingFragmentBinding>() {

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): LoginLoadingFragmentBinding {
        return LoginLoadingFragmentBinding.inflate(inflater, root, attachToRoot)
    }

    override fun setupViews() = Unit
    override fun injectToGraph(appComponent: AppComponent) = Unit
    override fun bindViewModel() = Unit
    override fun unbindViewModel() = Unit

    companion object {

        fun newInstance() = LoginLoadingFragment()
    }
}