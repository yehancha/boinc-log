package com.example.boinclog.background

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.boinclog.MainActivity
import com.example.boinclog.R
import com.example.boinclog.utils.CHANNEL_ID_BOINC_NOTIFICATIONS
import com.example.boinclog.utils.LocalData
import com.example.boinclog.utils.RpcClientFactory

const val NAME_MESSAGE_CHECKER = "NAME_MESSAGE_CHECKER"
const val NOTIFICATION_ID = 1000

class MessageChecker(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    override fun doWork(): Result = notifyNewMessages()

    private fun notifyNewMessages(): Result {
        try {
            val lastSeqNo = LocalData(applicationContext).getLastSeqNo()

            val rpcClient = RpcClientFactory.getClient()
            val currentSeqNo = rpcClient.messageCount

            if (lastSeqNo < currentSeqNo) {
                showNotification(lastSeqNo, currentSeqNo)
            }

            logLastCheck()

            rpcClient.close()

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }

    private fun showNotification(lastSeqNo: Int, currentSeqNo: Int) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID_BOINC_NOTIFICATIONS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("New Boinc Messages")
            .setContentText("You have " + (currentSeqNo - lastSeqNo) + " new message(s).")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            // notificationId is a unique int for each notification that you must define
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun logLastCheck() {
        LocalData(applicationContext).setLastChecked(System.currentTimeMillis())
    }
}