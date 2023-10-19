package com.handy.android.lda

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import com.handy.android.lda.ui.screens.Settings
import com.handy.android.lda.ui.theme.LucidDreamAlarmTheme
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var alarmManager: AlarmManager // активатор действий по определенному расписанию

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        setContent {
            LucidDreamAlarmTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Settings(alarmManager)
                }
            }
        }
    }
}
