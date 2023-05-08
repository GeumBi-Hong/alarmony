package com.slembers.alarmony.feature.alarm

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.graphics.Typeface
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter.State.Empty.painter
import coil.compose.rememberImagePainter
import com.slembers.alarmony.R
import com.slembers.alarmony.feature.alarm.AlarmNoti.cancelNotification
import com.slembers.alarmony.feature.alarm.AlarmNoti.runNotification
import com.slembers.alarmony.feature.alarm.ui.theme.AlarmonyTheme
import com.slembers.alarmony.feature.common.ui.theme.toColor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AlarmActivity : ComponentActivity() {

    lateinit var wakeLock: PowerManager.WakeLock
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val alarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("alarm", Alarm::class.java) as Alarm
        } else {
            intent.getParcelableExtra<Alarm>("alarm") as Alarm
        }
        var alarmStartTime = calAlarm(alarm)

        runNotification(this, alarm)

        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                    acquire()
                }
            }

        // 강제로 화면 키기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        } else {
            this.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                (WindowManager.LayoutParams.FLAG_FULLSCREEN or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            )
        }
        val keyguardMgr = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardMgr.requestDismissKeyguard(this, null)
        }

        setContent {
            AlarmScreen(alarm, alarmStartTime)
        }
    }
    override fun onDestroy() {
        wakeLock.release()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(alarm : Alarm, alarmStartTime : Long) {
    val context = LocalContext.current as Activity
    val isClicked5 = remember { mutableStateOf(false)  }
    val isClicked10 = remember { mutableStateOf(false)  }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = "#66D5ED".toColor(),
        content = { innerPadding ->
//            BoxWithConstraints(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.backsun),
//                    contentDescription = null,
//                    contentScale = ContentScale.FillBounds,
//                    modifier = Modifier.fillMaxSize()
//                )
//                // 여기에 다른 콘텐츠 추가
//            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DrawCircle(alarm)
                Spacer(modifier = Modifier.height(100.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Button(
                        onClick = { isClicked5.value = true },
                        shape = CircleShape,
                        modifier = Modifier
                            .padding(5.dp)
                            .size(90.dp),
                        colors = ButtonDefaults.buttonColors("#FFDA8C".toColor())
                    ) {
                        Text(
                            text = "5분",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize =20.sp
                        )
                    }

                    Button(
                        onClick = {
                            val alarmEndTime = System.currentTimeMillis()
                            val alarmRemainTime = alarmEndTime - alarmStartTime // 알람 끄기 까지 걸린 시간
                            cancelNotification()
                            context.finish()
                            // 정지 누르면 끝 시간 api
                        },
                        shape = CircleShape,
                        border = BorderStroke(10.dp, "#63B1C2".toColor()),
                        modifier = Modifier
                            .padding(5.dp)
                            .size(130.dp),
                        colors = ButtonDefaults.buttonColors("#FFDA8C".toColor())
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Stop,
                            contentDescription = "Setting",
                            tint = "#EF2828".toColor(),
                            modifier = Modifier.size(80.dp)
                        )
                    }
                    Button(
                        onClick = { isClicked10.value = true },
                        shape = CircleShape,
                        modifier = Modifier
                            .padding(5.dp)
                            .size(90.dp),
                        colors = ButtonDefaults.buttonColors("#FFDA8C".toColor())
                    ) {
                        Text(
                            text = "10분",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize =20.sp
                        )
                    }
                }
            }
            if (isClicked5.value) {
                SnoozeNoti(5, isClicked5, context)
            }
            if (isClicked10.value) {
                SnoozeNoti(10, isClicked10, context)
            }
        }
    )
}

@Composable
fun DrawCircle(alarm : Alarm) {
    val hour = if (alarm.hour.toString().length == 1) { "0" + alarm.hour.toString() }
    else {
        alarm.hour.toString()
    }
    val minute = if (alarm.minute.toString().length == 1) { "0" + alarm.minute.toString() }
    else {
        alarm.minute.toString()
    }
    Canvas(
        modifier = Modifier.size(300.dp)
    ) {
        val outerRadius = size.width / 2
        drawCircle(
            color = "#EDEDED".toColor().copy(alpha = 0.9f),
            radius = outerRadius,
            center = center,
            style = Stroke(width = 20.dp.toPx())
        )

        drawCircleWithInnerCircle(center, outerRadius / 1.04f, "#F3F3F3".toColor().copy(alpha = 0.9f))
        drawCircleWithInnerCircle(center, outerRadius / 1.2f, Color.White.copy(alpha = 0.9f))

        drawIntoCanvas { canvas ->
            val text = "${hour}:${minute}"
            val typeface = Typeface.create("font/roboto_bold.ttf", Typeface.BOLD)
            val paint1 = Paint().asFrameworkPaint().apply {
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = 75.sp.toPx()
                color = Color.Black.toArgb()
                setTypeface(typeface)
                isAntiAlias = true
            }
            val calendar: Calendar = Calendar.getInstance()
            var todayDayOfWeek: Int = calendar.get(Calendar.DAY_OF_WEEK)
            val todayDay = when(todayDayOfWeek) {
                1 -> "일"
                2 -> "월"
                3 -> "화"
                4 -> "수"
                5 -> "목"
                6 -> "금"
                7 -> "토"
                else -> ""
            }
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("MM/dd")
            val formatted = current.format(formatter)
            val text2 = "${formatted} (${todayDay})"
            val paint2 = Paint().asFrameworkPaint().apply {
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = 30.sp.toPx()
                color = Color.Black.toArgb()
                setTypeface(typeface)
                isAntiAlias = true
            }
            val y1 = center.y - ((paint1.descent() + paint1.ascent()) / 2)
            canvas.nativeCanvas.drawText(text, center.x, y1, paint1)
            canvas.nativeCanvas.drawText(text2, center.x, center.y-120, paint2)
        }
    }
}

fun DrawScope.drawCircleWithInnerCircle(center: Offset, innerRadius: Float, innerColor: Color) {

    drawCircle(innerColor, radius = innerRadius - 4.dp.toPx(), center)
}

@Preview
@Composable
fun DefaultView() {
    val alarm = Alarm(
        0,
        "장덕모임",
        8,
        45,
        listOf(true, true, true, false, false, true, true),
        "자장가",
        15,
        true
    )
    AlarmScreen(alarm = alarm, alarmStartTime = 0L)
}