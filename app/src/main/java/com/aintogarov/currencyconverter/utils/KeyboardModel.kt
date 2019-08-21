package com.aintogarov.currencyconverter.utils

import android.view.ViewTreeObserver
import androidx.annotation.CallSuper
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable

class KeyboardModel : LifecycleObserver {
    var activity: FragmentActivity? = null
    private val isOpenRelay: BehaviorRelay<Boolean> = BehaviorRelay.createDefault(false)

    private val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
        private var lastState: Boolean = activity?.isKeyboardOpen() ?: false

        override fun onGlobalLayout() {
            val isOpen = activity?.isKeyboardOpen() ?: false
            if (isOpen == lastState) {
                return
            } else {
                dispatchKeyboardEvent(isOpen)
                lastState = isOpen
            }
        }
    }

    private fun registerKeyboardListener() {
        activity?.getRootView()?.viewTreeObserver?.addOnGlobalLayoutListener(listener)
    }

    private fun dispatchKeyboardEvent(isOpen: Boolean) {
        when {
            isOpen -> isOpenRelay.accept(true)
            !isOpen -> isOpenRelay.accept(false)
        }
    }

    fun observe(): Observable<Boolean> = isOpenRelay

    @OnLifecycleEvent(value = Lifecycle.Event.ON_RESUME)
    @CallSuper
    fun onResume() {
        activity?.let { dispatchKeyboardEvent(it.isKeyboardOpen()) }
        registerKeyboardListener()
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_PAUSE)
    @CallSuper
    fun onPause() {
        unregisterKeyboardListener()
    }

    private fun unregisterKeyboardListener() {
        activity?.getRootView()?.viewTreeObserver?.removeOnGlobalLayoutListener(listener)
    }
}

