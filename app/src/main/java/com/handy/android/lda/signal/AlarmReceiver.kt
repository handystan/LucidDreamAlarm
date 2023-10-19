package com.handy.android.lda.signal

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.handy.android.lda.ui.screens.cancelAlarm
import com.handy.android.lda.ui.screens.enableAlarmMS
import com.handy.android.lda.ui.screens.notificationId
import com.handy.android.lda.workers.AlarmWorker

/**
 * класс, который выполняет действие при срабатывании будильника
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        //ставим будильник
        if (intent.action == null) {
            val notificationId = intent.getIntExtra("notificationId", 0)
            val notificationManager = getSystemService(context, NotificationManager::class.java) as NotificationManager
            notificationManager.cancel(notificationId)
            Log.d("myLogs", "notificationId $notificationId is cancelled")

            val workRequest = OneTimeWorkRequestBuilder<AlarmWorker>()
            val data = Data.Builder()
            data.putBoolean("isLooping", true)
            workRequest.setInputData(data.build())
            WorkManager.getInstance(context).enqueue(workRequest.build())
        } else if(intent.action.equals("stopAlarm")) { //останавливаем будильник из Notification
            enableAlarmMS.value = false
            cancelAlarm(context) // отмена ранее установленного будильника
        }
    }
}