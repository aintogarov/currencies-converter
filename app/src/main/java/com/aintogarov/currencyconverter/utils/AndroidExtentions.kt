package com.aintogarov.currencyconverter.utils

import android.graphics.Rect
import android.view.View
import androidx.fragment.app.FragmentActivity


fun FragmentActivity.getRootView(): View {
    return findViewById(android.R.id.content)
}

fun FragmentActivity.isKeyboardOpen(): Boolean {
    val visibleBounds = Rect()
    getRootView().getWindowVisibleDisplayFrame(visibleBounds)

    val ratio = visibleBounds.height() / visibleBounds.width().toFloat()
    return ratio < 1.3
}