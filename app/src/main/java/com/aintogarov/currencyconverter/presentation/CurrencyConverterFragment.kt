package com.aintogarov.currencyconverter.presentation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aintogarov.currencyconverter.R
import com.aintogarov.currencyconverter.di.Injector
import com.aintogarov.currencyconverter.domain.CurrencyAmount
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers


class CurrencyConverterFragment : Fragment() {

    private lateinit var currenciesRecyclerView: RecyclerView
    private lateinit var viewModel: CurrenciesViewModel

    private val handler = Handler(Looper.getMainLooper())
    private val currencyClicks: Relay<CurrencyAmount> = PublishRelay.create()
    private val viewDisposable: CompositeDisposable = CompositeDisposable()

    private val adapter = CurrenciesAdapter(clickListener = currencyClicks::accept)

    private val requestFocusRunnable = object : Runnable {
        override fun run() {
            val viewHolder = currenciesRecyclerView.findViewHolderForAdapterPosition(0)
            if (viewHolder != null && viewHolder.adapterPosition == 0) {
                currenciesRecyclerView.scrollToPosition(0)
                viewHolder.itemView.requestFocus()
            } else {
                handler.postDelayed(this, UPDATE_SCROLL_AND_FOCUS_DELAY)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appComponent = Injector.appComponent
        viewModel = CurrenciesViewModel(
            currenciesModel = appComponent.currenciesModel(),
            currencyClicks = currencyClicks,
            workerScheduler = Schedulers.computation(),
            uiScheduler = AndroidSchedulers.mainThread()
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_currency_converter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currenciesRecyclerView = view.findViewById(R.id.currencies_recycler_view)
        currenciesRecyclerView.layoutManager = LinearLayoutManager(context)
        currenciesRecyclerView.adapter = adapter
        currenciesRecyclerView.setHasFixedSize(true)
    }

    override fun onStart() {
        super.onStart()
        viewModel.start()
    }

    override fun onResume() {
        super.onResume()
        viewDisposable += viewModel.currenciesWithDiff()
            .subscribe { adapter.applyItems(it.currenciesList, it.diffResult) }

        viewDisposable += viewModel.observeReordering()
            .subscribe { handler.postDelayed(requestFocusRunnable, UPDATE_SCROLL_AND_FOCUS_DELAY) }
    }

    override fun onPause() {
        super.onPause()
        viewDisposable.clear()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stop()
    }

    private companion object {
        const val UPDATE_SCROLL_AND_FOCUS_DELAY = 8L
    }
}