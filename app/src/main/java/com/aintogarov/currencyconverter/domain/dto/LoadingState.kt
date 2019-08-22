package com.aintogarov.currencyconverter.domain.dto


sealed class LoadingState {
    object Loaded : LoadingState()
    object Loading : LoadingState()
    object Error : LoadingState()
}