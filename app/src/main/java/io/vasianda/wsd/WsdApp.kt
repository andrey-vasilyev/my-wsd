package io.vasianda.wsd

import android.app.Application
import dagger.Binds
import dagger.Component
import dagger.Module
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface ApplicationGraph {
    fun inject(activity: MainActivity)

    fun inject(widgetProvider: BalanceAppWidgetProvider)
}

class WsdApp : Application() {
    val applicationGraph: ApplicationGraph = DaggerApplicationGraph.create()
}

@Module
abstract class AppModule {

   @Binds
   abstract fun balanceService(balanceService: RemoteBalanceService): BalanceService
}
