package com.aintogarov.currencyconverter

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.aintogarov.currencyconverter.di.Injector
import com.aintogarov.currencyconverter.domain.RatesModel


class MainActivity : FragmentActivity() {

    private lateinit var ratesModel: RatesModel

    override fun onCreate(savedInstanceState: Bundle?) {
        ratesModel = Injector.appComponent.ratesModel()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        ratesModel.startUpdates()
    }

    override fun onStop() {
        super.onStop()
        ratesModel.stopUpdates()
    }
}