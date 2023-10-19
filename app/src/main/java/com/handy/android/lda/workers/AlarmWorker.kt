package com.handy.android.lda.workers

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.handy.android.lda.signal.startSignal
import com.handy.android.lda.ui.screens.*

class AlarmWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    val context = ctx
    override suspend fun doWork(): Result {
        return try {
            // отображение activity после срабатывания будильника
            val intent1 = Intent(context, AlarmWorkedActivity::class.java)
            intent1.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            ContextCompat.startActivity(context, intent1, null)
            isFinishedAlarm = false
            timeIsLaunchedAW = false
            Log.d("myLogs", "Activity is showed")
            // запуск сигнала
            startSignal(
                context,
                signalEnabledS,
                vibrationEnabledS,
                amountS,
                durationS,
                intervalS,
                inputData.getBoolean("isLooping", true),
            )
            // посылаем сообщение, что AlarmWorkedActivity нужно закрывать
            val intent = Intent("finishAlarmWorkedActivity")
            context.sendBroadcast(intent)
            Result.success()
        } catch (e: java.lang.Exception) {
            Log.e("myLogs", e.toString())
            Result.failure()
        }
    }
}