package com.handy.android.lda.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.util.*

var isFinishedAlarm = false // закончил работу будильник или нет
var timeIsLaunchedAW = false // запущен поток с отображением текущего времени

//форма, открывающаяся при срабатывании будильника
@Preview(showBackground = true)
@Composable
fun AlarmWorked() {
    enableAlarmMS.value = false
    val activity = (LocalContext.current as? Activity)
    val context = LocalContext.current
    val curTime = remember { mutableStateOf("00:00") } // текущее время

    // асинхронная функция для отображения текущего времени
    fun CoroutineScope.launchUpdateTimeAsync(repeatMillis: Long, action: () -> Unit) = this.async {
        while (!isFinishedAlarm) {
            action()
            delay(repeatMillis)
        }
    }
    // собственно сам вызов асинхронной функции для отображения текущего времени
    if (!timeIsLaunchedAW && !isFinishedAlarm) {
        CoroutineScope(Dispatchers.Main).launchUpdateTimeAsync(996) {
            curTime.value = "${Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')}:" +
                    Calendar.getInstance().get(Calendar.MINUTE).toString().padStart(2, '0')
        }
        timeIsLaunchedAW = true
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = curTime.value,
            style = MaterialTheme.typography.h2
        )
        Text("Будильник", style = MaterialTheme.typography.h3)
        Button(modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
            onClick = {
                cancelAlarm(context) // отмена ранее установленного будильника
                isFinishedAlarm = true
                timeIsLaunchedAW = false
                activity?.finish() // закрываем activity
            }
        ) {
            Text(text = "Остановить", style = MaterialTheme.typography.h3)
        }
    }
}
