package com.aintogarov.currencyconverter

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.aintogarov.currencyconverter.di.Injector
import com.aintogarov.currencyconverter.domain.CurrenciesModel
import com.aintogarov.currencyconverter.domain.RatesModel


class MainActivity : FragmentActivity() {

    private lateinit var ratesModel: RatesModel
    private lateinit var currenciesModel: CurrenciesModel

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        ratesModel = Injector.appComponent.ratesModel()
        currenciesModel = Injector.appComponent.currenciesModel()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        ratesModel.startUpdates()
        currenciesModel.onStart()
    }

    override fun onStop() {
        super.onStop()
        currenciesModel.onStop()
        ratesModel.stopUpdates()
    }
}