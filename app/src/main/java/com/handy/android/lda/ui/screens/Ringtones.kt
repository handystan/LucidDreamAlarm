package com.handy.android.lda.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.handy.android.lda.R
import com.handy.android.lda.signal.listOfRingtones
import com.handy.android.lda.signal.ringtoneUri
import com.handy.android.lda.signal.startSignal
import com.handy.android.lda.signal.stopSignal
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

var jobRingtone: Job? = null // Job для проигрывания рингтона
var mapOfRingtones: SortedMap<String, Uri>? = null

//@Preview(showBackground = true)
@Composable
fun Ringtones(visibility: MutableState<Boolean>, setRingtone: (Uri?) -> Unit) {
    val scope = rememberCoroutineScope() //нужно для запуска параллельных процессов
    val context = LocalContext.current
    val visibleRingtones = remember { visibility }
    if (mapOfRingtones == null) mapOfRingtones = listOfRingtones(context)
    val selectedOption = remember { mutableStateOf(mapOfRingtones?.firstKey()) }

    //обработка нажатия кнопки "Назад"
    BackHandler(enabled = visibleRingtones.value) {
        closeRingtones(visibleRingtones, setRingtone, selectedOption)
    }

    AnimatedVisibility(visibleRingtones.value) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            //backgroundColor = Purple200,
            elevation = 10.dp, shape = RoundedCornerShape(15.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        closeRingtones(visibleRingtones, setRingtone, selectedOption)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                            contentDescription = "back"
                        )
                    }
                    Text(
                        text = "Выбор мелодии",
                        style = TextStyle(fontSize = 20.sp),
                    )
                }
                LazyColumn(
                    Modifier.selectableGroup()
                    //.verticalScroll(rememberScrollState())
                ) {
                    itemsIndexed(mapOfRingtones!!.keys.toList()) { _, text ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(selected = (text == selectedOption.value), onClick = {
                                    selectedOption.value = text
                                    Log.d("myLogs", "jobRingtone is null = ${jobRingtone == null}")
                                    Log.d("myLogs", "jobRingtone?.isActive = ${jobRingtone?.isActive}")
                                    jobRingtone?.cancel()
                                    stopSignal()
                                    jobRingtone = scope.launch {
                                        ringtoneUri = mapOfRingtones!![text]
                                        startSignal(context, true, false, "1", "0", "0", false)
                                    }
                                }), verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                modifier = Modifier.padding(start = 10.dp),
                                selected = (text == selectedOption.value),
                                onClick = null
                            )
                            Text(modifier = Modifier.padding(start = 10.dp), text = text, fontSize = 22.sp)
                        }
                    }
                }
            }
        }
    }
}

//закрытие списков рингтонов
fun closeRingtones(
    visibleRingtones: MutableState<Boolean>, setRingtone: (Uri?) -> Unit, selectedOption: MutableState<String?>
) {
    setRingtone(mapOfRingtones!![selectedOption.value])
    visibleRingtones.value = false
    jobRingtone?.cancel()
    stopSignal()
}