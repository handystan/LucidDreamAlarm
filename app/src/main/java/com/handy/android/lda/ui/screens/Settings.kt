package com.handy.android.lda.ui.screens

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings.canDrawOverlays
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.handy.android.lda.R
import com.handy.android.lda.signal.AlarmReceiver
import com.handy.android.lda.signal.ringtoneUri
import com.handy.android.lda.signal.stopSignal
import com.handy.android.lda.utils.prefs
import kotlinx.coroutines.*
import java.util.*

var alarmManager: AlarmManager? = null // активатор действий по определенному расписанию
lateinit var pendingIntent: PendingIntent
var notificationId = 0 // id уведомления по будильнику
var enableAlarmMS =
    mutableStateOf(prefs.enableAlarmPref) // переменная для принудительного выключения будильника из AlarmWorked
var signalEnabledS = prefs.signalEnabledPref // установлен ли сигнал
var vibrationEnabledS = prefs.vibrationEnabledPref // установлен ли вибросигнла
var amountS = prefs.amountPref // количество сигналов
var durationS = prefs.durationPref // продолжительность сигнала в сек.
var intervalS = prefs.intervalPref // интервал между сигналами в сек.
private var timeIsLaunched = false // запущен поток с отображением оставшегося до звонка времени или нет

//@Preview(showBackground = true)
@Composable
fun Settings(am: AlarmManager) {
    alarmManager = am // активатор действий по определенному расписанию
    val context = LocalContext.current
    val enableAlarm = remember { enableAlarmMS } // включен будильник или нет
    // инициация pendingIntent
    if (enableAlarmMS.value == true) {
        // установление BroadcastReceiver, в котором будут происходить действия в момент срабатывания будильника
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("notificationId", notificationId)
        pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    val visibleRingtones = remember { mutableStateOf(false) } // показывать окно с выбором мелодий или нет
    val visibleAlertDialog =
        remember { mutableStateOf(false) } // показывать AlertDialog с настройкой отображения приложения поверх других окон
    val hour = remember { mutableStateOf(prefs.hourPref) } //установленный час в будильнике
    val minute = remember { mutableStateOf(prefs.minutePref) } //установленная минута в будильнике
    val signalEnabled = remember { mutableStateOf(signalEnabledS) } // установлен ли сигнал
    val vibrationEnabled = remember { mutableStateOf(vibrationEnabledS) } // установлен ли вибросигнла
    val amount = remember { mutableStateOf(amountS) } // количество сигналов
    val duration = remember { mutableStateOf(durationS) } // продолжительность сигнала в сек.
    val interval = remember { mutableStateOf(intervalS) } // интервал между сигналами в сек.
    val enableButtonRington = remember { mutableStateOf(true) } // Доступна кнопка с выбором мелодий или нет
    // название мелодии сигнала
    val ringtoneName = remember { mutableStateOf(prefs.ringtoneNamePref) }
    val timeTillSignal = remember { mutableStateOf("Сигнал будильника выключен") } // сколько времени до сигнала

    // асинхронная функция для заполнения оставшегося времени до звонка будильника
    fun CoroutineScope.launchPeriodicAsync(repeatMillis: Long, action: () -> Unit) = this.async {
        while (enableAlarm.value) {
            action()
            delay(repeatMillis)
        }
    }
    // собственно сам вызов асинхронной функции для заполнени оставшегося времени до звонка будильника
    if (!timeIsLaunched && enableAlarm.value) {
        CoroutineScope(Dispatchers.IO).launchPeriodicAsync(996) {
            timeTillSignal.value = timePeriodToAlarm(alarmInCalendar(hour.value, minute.value))
        }
        timeIsLaunched = true
    } else if (!enableAlarm.value) {
        timeTillSignal.value = "Сигнал будильника выключен"
    }

    //переменная для выбора времени
    val timePickerDialog = TimePickerDialog(
        context, { view, _hourOfDay, _minute ->
            hour.value = _hourOfDay
            prefs.hourPref = _hourOfDay
            minute.value = _minute
            prefs.minutePref = _minute
            // отмена ранее установленного будильника
            cancelAlarm(context)
            signalEnabledS = signalEnabled.value
            vibrationEnabledS = vibrationEnabled.value
            amountS = amount.value
            durationS = duration.value
            intervalS = interval.value
            // устанавливаем будильник
            setAlarm(context, hour.value, minute.value)
            if (!canDrawOverlays(context)) {
                visibleAlertDialog.value = true
            }
        }, hour.value, minute.value, true
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(modifier = Modifier
                .weight(1f, false)
                .alpha(0f), checked = true, onCheckedChange = {})
            Text(
                modifier = Modifier.weight(5f),
                text = "Настройки будильника",
                style = TextStyle(fontSize = 20.sp, textAlign = TextAlign.Center)
            )
            //Включен будильник или нет
            Switch(modifier = Modifier.weight(1f, false), checked = enableAlarm.value, onCheckedChange = {
                enableAlarm.value = it
                prefs.enableAlarmPref = it
                if (enableAlarm.value) {
                    signalEnabledS = signalEnabled.value
                    vibrationEnabledS = vibrationEnabled.value
                    amountS = amount.value
                    durationS = duration.value
                    intervalS = interval.value
                    // устанавливаем будильник
                    setAlarm(context, hour.value, minute.value)
                    if (!canDrawOverlays(context)) {
                        visibleAlertDialog.value = true
                    }
                } else {
                    // отмена будильника
                    cancelAlarm(context)
                }
            })
        }
        Text(
            modifier = Modifier.padding(bottom = 10.dp),
            text = timeTillSignal.value,
            style = TextStyle(fontSize = 12.sp)
        )
        TextButton(
            modifier = Modifier.padding(bottom = 10.dp),
            onClick = { timePickerDialog.show() },
            enabled = enableAlarm.value
        ) {
            Text(
                text = "${hour.value.toString().padStart(2, '0')}:${minute.value.toString().padStart(2, '0')}",
                fontSize = 50.sp
            )
        }
        OutlinedTextField(modifier = Modifier.onFocusChanged {
            if (!it.isFocused && (duration.value.isEmpty() || duration.value == "0")) {
                duration.value = "1"
                prefs.durationPref = "1"
                durationS = "1"
            }
        },
            value = duration.value,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text("Продолжительность сигнала, сек.") },
            enabled = enableAlarm.value,
            onValueChange = {
                if (it.isEmpty() || it.matches(Regex("^\\d+\$"))) {
                    duration.value = it
                    prefs.durationPref = it
                    durationS = it
                }
            })
        OutlinedTextField(modifier = Modifier
            .onFocusChanged {
                if (!it.isFocused && (interval.value.isEmpty() || interval.value == "0")) {
                    interval.value = "1"
                    prefs.intervalPref = "1"
                    intervalS = "1"
                }
            }
            .padding(top = 10.dp),
            value = interval.value,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text("Интервал, сек.") },
            enabled = enableAlarm.value,
            onValueChange = {
                if (it.isEmpty() || it.matches(Regex("^\\d+\$"))) {
                    interval.value = it
                    prefs.intervalPref = it
                    intervalS = it
                }
            })
        OutlinedTextField(modifier = Modifier
            .onFocusChanged {
                if (!it.isFocused && (amount.value.isEmpty() || amount.value == "0")) {
                    amount.value = "1"
                    prefs.amountPref = "1"
                    amountS = "1"
                }
            }
            .padding(top = 10.dp),
            value = amount.value,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text("Количество сигналов") },
            enabled = enableAlarm.value,
            onValueChange = {
                if (it.isEmpty() || it.matches(Regex("^\\d+\$"))) {
                    amount.value = it
                    prefs.amountPref = it
                    amountS = it
                }
            })
        // включение и выключение звукового сигнала и кнопка выбора мелодии
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .selectable(selected = !signalEnabled.value, onClick = {
                        if (enableAlarm.value) {
                            signalEnabled.value = !signalEnabled.value
                            prefs.signalEnabledPref = signalEnabled.value
                            enableButtonRington.value = signalEnabled.value
                            signalEnabledS = signalEnabled.value
                            if (!signalEnabled.value) {
                                vibrationEnabled.value = true
                                prefs.vibrationEnabledPref = true
                            }
                        }
                    }), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = signalEnabled.value, enabled = enableAlarm.value, onCheckedChange = null
                )
                Text(text = "Сигнал")
            }
            Button(
                onClick = {
                    visibleRingtones.value = true
                }, enabled = enableAlarm.value && enableButtonRington.value
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Мелодия сигнала", fontSize = 14.sp)
                    Text(text = ringtoneName.value, fontSize = 10.sp)
                }
            }
        }
        // включение и выключение вибрации
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
                .selectable(selected = !vibrationEnabled.value, onClick = {
                    if (enableAlarm.value) {
                        vibrationEnabled.value = !vibrationEnabled.value
                        prefs.vibrationEnabledPref = vibrationEnabled.value
                        vibrationEnabledS = vibrationEnabled.value
                        if (!vibrationEnabled.value) {
                            signalEnabled.value = true
                            prefs.signalEnabledPref = true
                            enableButtonRington.value = true
                        }
                    }
                }), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = vibrationEnabled.value, enabled = enableAlarm.value, onCheckedChange = null
            )
            Text(text = "Вибрация")
        }
    }
    // окно со списком рингтонов
    Ringtones(visibleRingtones, setRingtone = { uri ->
        ringtoneUri = uri
        ringtoneName.value = RingtoneManager.getRingtone(context, uri).getTitle(context)
        prefs.ringtoneNamePref = ringtoneName.value
        prefs.ringtoneUriPref = ringtoneUri.toString()
    })
    // окна с вопросом о необходимости изменить настройки, чтобы появлялось окно с выключением сигнала даже при заблокированном экране
    SystemAlertWindowDialog(visibleAlertDialog)
}

// установка будильника
private fun setAlarm(context: Context, hour: Int, minute: Int) {
    // если не установлено разрешение на точное время будильника в фоновом режиме, то просим пользователя проставить его
    val packageName = context.packageName // Получаем пакетное имя вашего приложения
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName)
    if (!isIgnoringBatteryOptimizations) {
        val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(context, intent, null)
    }

    //время срабатывания будильника
    val alarmTime = alarmInCalendar(hour, minute)
    // установление BroadcastReceiver, в котором будут происходить действия в момент срабатывания будильника
    val intent = Intent(context, AlarmReceiver::class.java)

    //notificationId++
    intent.putExtra("notificationId", notificationId)
    pendingIntent = PendingIntent.getBroadcast(
        context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    //установление будильника
    alarmManager?.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP, alarmTime.timeInMillis, pendingIntent
    )
    // создание уведомления
    @SuppressLint("MissingPermission") if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
        // создание канала для уведомлений
        createNotificationChannel(context)
        // создаем pendingIntent для остановки будильника
        val stopIntent = Intent(context, AlarmReceiver::class.java)
        stopIntent.action = "stopAlarm"
        stopIntent.putExtra("notificationId", notificationId)
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        // создание самого уведомления
        val notificationBuilder = NotificationCompat.Builder(context, "alarmLucid").setContentTitle("Будильник")
            .setContentText("Установлен на ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}")
            .setSmallIcon(R.drawable.alarm_on).setPriority(NotificationCompat.PRIORITY_HIGH).setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).addAction(0, "Остановить", stopPendingIntent)

        with(NotificationManagerCompat.from(context)) {
            /*if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }*/
            this.notify(notificationId, notificationBuilder.build())
            Log.d("myLogs", "notificationId = ${notificationId}")
        }
    }
    Toast.makeText(context, timePeriodToAlarm(alarmTime), Toast.LENGTH_LONG).show()
    Log.d("myLogs", "Alarm is created: ${hour}:${minute}")
}

// отмена будильника
fun cancelAlarm(context: Context) {
    // инициация AlarmManager для кейса, когда отключение звонка происходит из окна отключения будильника
    if (alarmManager == null) {
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
    // отмена будильника
    try {
        alarmManager?.cancel(pendingIntent)
    } catch (e: UninitializedPropertyAccessException) {
        // инициация pendingIntent
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("notificationId", notificationId)
        pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager?.cancel(pendingIntent)
    }
    // отменя уведомления
    if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
        val notificationManager = ContextCompat.getSystemService(
            context, NotificationManager::class.java
        ) as NotificationManager
        notificationManager.cancel(notificationId)
    }
    // остановка сигнала и виброзвонка
    stopSignal()
    timeIsLaunched = false
    Log.d("myLogs", "alarm with notificationId = ${notificationId} is cancelled")
}

// создание канала для уведомлений
private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Alarm"
        val descriptionText = "Alarm Notification"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("alarmLucid", name, importance).apply {
            description = descriptionText
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

// время срабатывания будильника в формате Calendar
private fun alarmInCalendar(hour: Int, minute: Int): Calendar {
    val alarmCalendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    // если установленное время меньше текущего времени, значит будельник устанавливается на это время следующего дня
    if (alarmCalendar.timeInMillis < Calendar.getInstance().timeInMillis) {
        alarmCalendar.add(Calendar.DAY_OF_YEAR, 1)
    }
    return alarmCalendar
}

// определение времени, через сколько прозвенит будильник
private fun timePeriodToAlarm(alarmTime: Calendar): String {
    val timeInterval = alarmTime.timeInMillis - Calendar.getInstance().timeInMillis
    val amountDays = timeInterval / 1000 / 60 / 60 / 24
    val amountHours = (timeInterval - amountDays * 1000 * 60 * 60 * 24) / 1000 / 60 / 60
    val amountMinutes = (timeInterval - amountDays * 1000 * 60 * 60 * 24 - amountHours * 1000 * 60 * 60) / 1000 / 60
    var message = "Звонок "
    if (amountDays == 0L && amountHours == 0L && amountMinutes == 0L) {
        message = message + "менее через 1 минуту"
    } else {
        message =
            message + "через ${if (amountDays > 0) "$amountDays дня " else ""}" + (if (amountHours > 0) "$amountHours час. " else "") + (if (amountMinutes > 0) "$amountMinutes мин." else "")
    }
    return message
}