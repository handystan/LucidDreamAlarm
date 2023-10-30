package com.handy.android.lda.signal

import android.content.Context
import android.database.Cursor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.handy.android.lda.utils.prefs
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.*

var vibrator: Vibrator? = null
var player: MediaPlayer? = null
var ringtoneUri = Uri.parse(prefs.ringtoneUriPref) // uri мелодии сигнала
var signalIsStopped = false // нужно ли в данный момент остановить работу будильника

// воспроизведение самого сигнала
suspend fun startSignal(
    context: Context,
    signalEnabled: Boolean,
    vibrationEnabled: Boolean,
    amount: String,
    duration: String,
    interval: String,
    isLooping: Boolean
) {
    signalIsStopped = false
    repeat(amount.toInt()) { i ->
        if (!signalIsStopped) {
            if (signalEnabled) {
                player = MediaPlayer()
                try {
                    player?.setDataSource(context, ringtoneUri)
                    // привязываем громкость к громкости будильника
                    player?.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    player?.prepare()
                    player?.setLooping(isLooping)
                    player?.start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (vibrationEnabled) {
                vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(100, 100), 0))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(100, 100), 0)
                }
            }
            if (isLooping) {
                delay(duration.toLong() * 1000L) // продолжительность сигнала
                stopSignal(false) // прекращаем работу будильника через заданное количество секунд
                delay(interval.toLong() * 1000L) // задержка между сигналами
            }
        }
    }
}

// остановка сигнала
fun stopSignal(isWholeStopSignal: Boolean = true) {
    if (isWholeStopSignal) signalIsStopped = true
    player?.stop()
    vibrator?.cancel()
}

// получение списка рингтонов
fun listOfRingtones(context: Context): SortedMap<String, Uri> {
    val sortedMap = sortedMapOf<String, Uri>()
    val manager = RingtoneManager(context)
    manager.setType(RingtoneManager.TYPE_ALL)
    val cursor: Cursor = manager.cursor
    while (cursor.moveToNext()) {
        sortedMap[cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)] = manager.getRingtoneUri(cursor.position)
    }
    return sortedMap
}