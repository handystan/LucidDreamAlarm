package com.handy.android.lda.utils

import android.content.Context
import android.content.SharedPreferences
import android.media.RingtoneManager

class Prefs(val context: Context) {
    private val enableAlarm = "enableAlarmPref" // включен будильник или нет
    private val hour = "hourPref" //установленный час в будильнике
    private val minute = "minutePref" //установленная минута в будильнике
    private val signalEnabled = "signalEnabledPref" // установлен ли сигнал
    private val vibrationEnabled = "vibrationEnabledPref" // установлен ли вибросигнла
    private val duration = "durationPref" // продолжительность сигнала в сек.
    private val interval = "intervalPref" // интервал между сигналами в сек.
    private val amount = "amountPref" // количество сигналов
    private val ringtoneName = "ringtoneNamePref" // название мелодии сигнала
    private val ringtoneUriStr = "ringtoneUriPref" // URI мелодии сигнала

    private val preferences: SharedPreferences = context.getSharedPreferences("lda", Context.MODE_PRIVATE)

    var enableAlarmPref: Boolean
        get() = preferences.getBoolean(enableAlarm, false)
        set(value) = preferences.edit().putBoolean(enableAlarm, value).apply()

    var minutePref: Int
        get() = preferences.getInt(minute, 30)
        set(value) = preferences.edit().putInt(minute, value).apply()

    var hourPref: Int
        get() = preferences.getInt(hour, 6)
        set(value) = preferences.edit().putInt(hour, value).apply()

    var vibrationEnabledPref: Boolean
        get() = preferences.getBoolean(vibrationEnabled, true)
        set(value) = preferences.edit().putBoolean(vibrationEnabled, value).apply()

    var signalEnabledPref: Boolean
        get() = preferences.getBoolean(signalEnabled, true)
        set(value) = preferences.edit().putBoolean(signalEnabled, value).apply()

    var durationPref: String
        get() = preferences.getString(duration, "3").toString()
        set(value) = preferences.edit().putString(duration, value).apply()

    var intervalPref: String
        get() = preferences.getString(interval, "5").toString()
        set(value) = preferences.edit().putString(interval, value).apply()

    var amountPref: String
        get() = preferences.getString(amount, "3").toString()
        set(value) = preferences.edit().putString(amount, value).apply()

    var ringtoneNamePref: String
        get() = preferences.getString(
            ringtoneName,
            RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .getTitle(context)
        ).toString()
        set(value) = preferences.edit().putString(ringtoneName, value).apply()

    var ringtoneUriPref: String
        get() = preferences.getString(
            ringtoneUriStr,
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()
        ).toString()
        set(value) = preferences.edit().putString(ringtoneUriStr, value).apply()
}