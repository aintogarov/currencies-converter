package com.aintogarov.currencyconverter.presentation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aintogarov.currencyconverter.R
import com.aintogarov.currencyconverter.di.Injector
import com.aintogarov.currencyconverter.domain.dto.CurrencyAmount
import com.aintogarov.currencyconverter.domain.dto.LoadingState
import com.aintogarov.currencyconverter.presentation.adapter.CurrenciesAdapter
import com.aintogarov.currencyconverter.presentation.dto.ClickEvent
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers


class CurrencyConverterFragment : Fragment() {

    private lateinit var currenciesRecyclerView: RecyclerView
    private lateinit var progressBar: ContentLoadingProgressBar
    private lateinit var retryButton: Button
    private lateinit var viewModel: CurrenciesViewModel

    private val handler = Handler(Looper.getMainLooper())
    private val currencyClicks: Relay<CurrencyAmount> = PublishRelay.create()
    private val amountInput: Relay<String> = PublishRelay.create()
    private val retryClicks: Relay<ClickEvent> = PublishRelay.create()
    private val viewDisposable: CompositeDisposable = CompositeDisposable()

    private val adapter = CurrenciesAdapter(
        clickListener = currencyClicks::accept,
        amountInputListener = amountInput::accept
    )

    private val requestFocusRunnable = object : Runnable {
        override fun run() {
            val viewHolder = currenciesRecyclerView.findViewHolderForAdapterPosition(0)
            if (viewHolder != null && viewHolder.adapterPosition == 0) {
                currenciesRecyclerView.scrollToPosition(0)
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
            amountInput = amountInput,
            retryClicks = retryClicks,
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

        progressBar = view.findViewById(R.id.progress_bar)

        retryButton = view.findViewById(R.id.retry_button)
        retryButton.setOnClickListener { retryClicks.accept(ClickEvent) }
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

        viewDisposable += viewModel.loadingState()
            .subscribe { state ->
                when (state) {
                    LoadingState.Loaded -> {
                        currenciesRecyclerView.visibility = View.VISIBLE
                        progressBar.hide()
                        retryButton.visibility = View.INVISIBLE
                    }
                    LoadingState.Loading -> {
                        currenciesRecyclerView.visibility = View.INVISIBLE
                        progressBar.show()
                        retryButton.visibility = View.INVISIBLE
                    }
                    LoadingState.Error -> {
                        currenciesRecyclerView.visibility = View.INVISIBLE
                        progressBar.hide()
                        retryButton.visibility = View.VISIBLE
                    }
                }
            }
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