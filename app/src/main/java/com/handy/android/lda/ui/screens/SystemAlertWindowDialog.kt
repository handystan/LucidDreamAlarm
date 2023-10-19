package com.handy.android.lda.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings.canDrawOverlays
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * функция для диалогового окна с вопросом о необходимости изменить настройки,
 * чтобы появлялось окно с выключением сигнала даже при заблокированном экране
 */
//
@Composable
fun SystemAlertWindowDialog(visibility: MutableState<Boolean>) {
    val context = LocalContext.current
    val openDialog = remember { visibility }
    // обработка результатов предоставления прав на отображение приложения поверх других окон
    val settingResultRequest =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (!canDrawOverlays(context)) {
                Toast.makeText(
                    context,
                    "Внимание! Для корректной работы данное разрешение необходимо!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            title = { Text(text = "Подтверждение действия") },
            text = { Text("Перейти в настройки для разрешения показывать приложение поверх других окон (для выключения сигнала)?") },
            buttons = {
                Row(
                    modifier = Modifier.padding(all = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                        onClick = {
                            openDialog.value = false
                            val intent = Intent(
                                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            settingResultRequest.launch((intent))
                        }
                    ) {
                        Text("Перейти")
                    }
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp),
                        onClick = { openDialog.value = false }
                    ) {
                        Text("Отмена")
                    }
                }
            }
        )
    }
}