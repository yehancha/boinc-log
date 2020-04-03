package com.example.boinclog.background

import android.app.IntentService
import android.content.Intent
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * TODO: Customize class - update intent actions and extra parameters.
 */
class MessageCheckerStarter : IntentService("MessageCheckerStarter") {

    override fun onHandleIntent(intent: Intent?) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val messageChecker = PeriodicWorkRequestBuilder<MessageChecker>(PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
            .setInitialDelay(PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(baseContext)
            .enqueueUniquePeriodicWork(NAME_MESSAGE_CHECKER, ExistingPeriodicWorkPolicy.REPLACE, messageChecker)
    }
}
