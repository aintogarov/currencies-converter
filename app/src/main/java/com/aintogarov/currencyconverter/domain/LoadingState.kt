package com.aintogarov.currencyconverter.domain


sealed class LoadingState {
    object Loaded : LoadingState()
    object Loading : LoadingState()
    object Error : LoadingState()
}