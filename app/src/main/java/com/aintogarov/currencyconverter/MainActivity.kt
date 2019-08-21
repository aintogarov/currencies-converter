package com.aintogarov.currencyconverter

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.aintogarov.currencyconverter.di.Injector
import com.aintogarov.currencyconverter.domain.CurrenciesModel
import com.aintogarov.currencyconverter.domain.MoneyAmountModel
import com.aintogarov.currencyconverter.domain.RatesModel
import com.aintogarov.currencyconverter.utils.KeyboardModel


class MainActivity : FragmentActivity() {

    private lateinit var ratesModel: RatesModel
    private lateinit var currenciesModel: CurrenciesModel
    private lateinit var amountModel: MoneyAmountModel
    private lateinit var keyboardModel: KeyboardModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        with(Injector.appComponent) {
            ratesModel = ratesModel()
            currenciesModel = currenciesModel()
            amountModel = amountModel()
            keyboardModel = keyboardModel()
        }
        keyboardModel.activity = this
        lifecycle.addObserver(keyboardModel)
    }

    override fun onStart() {
        super.onStart()
        ratesModel.onStart()
        currenciesModel.onStart()
        amountModel.onStart()
    }

    override fun onStop() {
        super.onStop()
        currenciesModel.onStop()
        ratesModel.onStop()
        amountModel.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        keyboardModel.activity = null
    }
}