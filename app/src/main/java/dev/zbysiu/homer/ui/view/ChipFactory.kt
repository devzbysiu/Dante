package dev.zbysiu.homer.ui.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.HapticFeedbackConstants
import dev.zbysiu.homer.core.book.BookLabel
import dev.zbysiu.homer.util.ColorUtils
import dev.zbysiu.homer.util.DanteUtils.dpToPixelF
import dev.zbysiu.homer.util.getBoldThemeFont
import dev.zbysiu.homer.util.isNightModeEnabled
import com.google.android.material.chip.Chip

object ChipFactory {

    fun buildChipViewFromLabel(
        context: Context,
        label: BookLabel,
        onLabelClickedListener: ((BookLabel) -> Unit)?,
        showCloseIcon: Boolean = false,
        closeIconClickCallback: ((BookLabel) -> Unit)? = null
    ): Chip {

        val chipColor = if (context.isNightModeEnabled()) {
            ColorUtils.desaturateAndDevalue(label.labelHexColor.asColorInt(), by = 0.25f)
        } else {
            label.labelHexColor.asColorInt()
        }

        return Chip(context).apply {
            chipBackgroundColor = ColorStateList.valueOf(Color.TRANSPARENT)
            text = label.title
            typeface = context.getBoldThemeFont()
            setTextColor(chipColor)
            chipStrokeColor = ColorStateList.valueOf(chipColor)
            chipStrokeWidth = context.dpToPixelF(1)
            setOnClickListener { v ->
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onLabelClickedListener?.invoke(label)
            }

            if (showCloseIcon) {
                closeIconTint = ColorStateList.valueOf(chipColor)
                isCloseIconVisible = true
                setOnCloseIconClickListener { v ->
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    closeIconClickCallback?.invoke(label)
                }
            }
        }
    }
}