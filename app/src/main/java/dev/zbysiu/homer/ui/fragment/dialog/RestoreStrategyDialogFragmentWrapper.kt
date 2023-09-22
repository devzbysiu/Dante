package dev.zbysiu.homer.ui.fragment.dialog

import android.content.Context
import dev.zbysiu.homer.databinding.DialogfragmentRestoreStrategyBinding
import dev.zbysiu.homer.util.DanteUtils.dpToPixelF
import dev.zbysiu.homer.util.RestoreStrategy
import dev.zbysiu.homer.util.layoutInflater
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView

/**
 * Author:  Martin Macheiner
 * Date:    30.08.2016
 */
class RestoreStrategyDialogFragmentWrapper {

    private var strategyListener: ((RestoreStrategy) -> Unit)? = null


    fun show(context: Context) {

        val vb = DialogfragmentRestoreStrategyBinding.inflate(context.layoutInflater())

        val dialog = MaterialDialog(context)
            .customView(view = vb.root)
            .cornerRadius(context.dpToPixelF(6))
            .cancelOnTouchOutside(true)

        setupViews(vb, dialog)

        dialog.show()
    }

    private fun setupViews(
        vb: DialogfragmentRestoreStrategyBinding,
        dialog: MaterialDialog
    ) {
        vb.dialogfragmentRestoreStrategyBtnMerge.setOnClickListener {
            strategyListener?.invoke(RestoreStrategy.MERGE)
            dialog.dismiss()
        }

        vb.dialogfragmentRestoreStrategyBtnOverwrite.setOnClickListener {
            strategyListener?.invoke(RestoreStrategy.OVERWRITE)
            dialog.dismiss()
        }
    }

    fun setOnRestoreStrategySelectedListener(
        listener: (RestoreStrategy) -> Unit
    ): RestoreStrategyDialogFragmentWrapper {
        return apply {
            this.strategyListener = listener
        }
    }

    companion object {

        fun newInstance(): RestoreStrategyDialogFragmentWrapper {
            return RestoreStrategyDialogFragmentWrapper()
        }
    }
}
