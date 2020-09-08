package io.vasianda.wsd

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.mapBoth
import kotlinx.coroutines.*
import javax.inject.Inject


class BalanceAppWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var balanceService: BalanceService

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        (context.applicationContext as WsdApp).applicationGraph.inject(this)
        val balance = GlobalScope.async(Dispatchers.IO) {
            val (login, password) = context.loginPassword()
            balanceService.getBalance(login, password)
        }
        GlobalScope.launch(Dispatchers.Main) {
            val balanceResult = withTimeoutOrNull(30_000) { balance.await() } ?: Err("Timeout")
            for (widgetId in appWidgetIds) {
                val remoteViews = RemoteViews(context.packageName, R.layout.balance_widget)
                remoteViews.setTextViewText(R.id.widget_text_view, balanceResult.mapBoth({ it.toString() }, { it }))
                appWidgetManager.updateAppWidget(widgetId, remoteViews)
            }
        }
    }
}