package com.handy.android.lda.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.handy.android.lda.ui.theme.LucidDreamAlarmTheme


class AlarmWorkedActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LucidDreamAlarmTheme (true) {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    AlarmWorked()
                }
            }
        }

        // регистрируем BroadcastReceiver, который будет сообщать, что нужно закрыть activity после окончания звонка
        val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val action = intent.action
                if (action == "finishAlarmWorkedActivity") {
                    isFinishedAlarm = true
                    timeIsLaunchedAW = false
                    unregisterReceiver(this)
                    finish()
                }
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("finishAlarmWorkedActivity"))

        // эти настрйоки позволяют отображаться actvity даже если экран заблокирован
        val win: Window = window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            win.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON // оставлять экран включенным всегда или через некоторое время выключать
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        } else {
            @Suppress("DEPRECATION")
            win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            @Suppress("DEPRECATION")
            win.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON // оставлять экран включенным всегда или через некоторое время выключать
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }
    }
}