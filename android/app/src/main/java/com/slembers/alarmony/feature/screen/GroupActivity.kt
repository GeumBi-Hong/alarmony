package com.slembers.alarmony.feature.screen

import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.commandiron.wheel_picker_compose.WheelTimePicker
import com.commandiron.wheel_picker_compose.core.TimeFormat
import com.commandiron.wheel_picker_compose.core.WheelPickerDefaults
import com.slembers.alarmony.feature.alarm.Alarm
import com.slembers.alarmony.feature.alarm.AlarmDto
import com.slembers.alarmony.feature.alarm.saveAlarm
import com.slembers.alarmony.feature.common.NavItem
import com.slembers.alarmony.feature.common.ui.compose.GroupCard
import com.slembers.alarmony.feature.common.ui.compose.GroupSubjet
import com.slembers.alarmony.feature.common.ui.compose.GroupTitle
import com.slembers.alarmony.feature.common.ui.theme.textColor
import com.slembers.alarmony.feature.common.ui.theme.toColor
import com.slembers.alarmony.feature.ui.common.AnimationRotation
import com.slembers.alarmony.feature.ui.common.CommonDialog
import com.slembers.alarmony.feature.ui.group.GroupBottomButtom
import com.slembers.alarmony.feature.ui.group.GroupInvite
import com.slembers.alarmony.feature.ui.group.GroupSound
import com.slembers.alarmony.feature.ui.group.GroupToolBar
import com.slembers.alarmony.feature.ui.group.GroupTypeButton
import com.slembers.alarmony.feature.ui.group.GroupVolume
import com.slembers.alarmony.model.db.SoundItem
import com.slembers.alarmony.model.db.dto.MemberDto
import com.slembers.alarmony.network.service.GroupService
import com.slembers.alarmony.util.DisplayDpUtil
import com.slembers.alarmony.util.Sound
import com.slembers.alarmony.util.groupSoundInfos
import com.slembers.alarmony.viewModel.GroupViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.LocalTime

@ExperimentalMaterial3Api
@ExperimentalGlideComposeApi
class GroupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window,false)
        setContent {
            val navController : NavHostController = rememberNavController()
            val viewModel by viewModels<GroupViewModel>()
            viewModel.onChangeSound(Sound())

            NavHost(
                navController = navController,
                startDestination = NavItem.Group.route
            ) {
                composable( route = NavItem.Group.route ) { GroupScreen(
                    navController = navController, viewModel = viewModel) }
                composable( route = NavItem.GroupInvite.route ) { InviteScreen(
                    navController = navController, viewModel = viewModel) }
                composable( route = NavItem.Sound.route ) { SoundScreen(
                    navController = navController, viewModel = viewModel) }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("GroupActivity","[그룹생성] Activity 시작")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("GroupActivity","[그룹생성] Activity 종료")
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
@ExperimentalMaterial3Api
@ExperimentalGlideComposeApi
fun GroupScreen(
    navController : NavHostController = rememberNavController(),
    viewModel : GroupViewModel = viewModel()
) {

    val title by viewModel.title.observeAsState()
    val timePickerState by viewModel.alarmTime.observeAsState()
    val isWeeks by viewModel.currentWeeks.observeAsState()
    val weeks = listOf("월","화","수","목","금","토","일")
    val members by viewModel.members.observeAsState()
    val soundName by viewModel.sound.observeAsState()
    val vibration by viewModel.vibrate.observeAsState()
    val soundVolume by viewModel.volumn.observeAsState()

    val scrollerState = rememberScrollState()
    val interaction = remember{ MutableInteractionSource() }
    val context = LocalContext.current

    // 초대된 그룹원 확인
    val checkedMember = navController.previousBackStackEntry?.savedStateHandle?.get<Set<MemberDto>>("checkedMember")
    Log.d("checked","[그룹생성] 선택한 멤버 : ${checkedMember.toString()}")
    val isClosed = remember { mutableStateOf(false) }
    val alertContext = remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    // 디스플레이의 너비를 구하는 변수
    val px = context.applicationContext.resources?.displayMetrics?.widthPixels
    val displayWidth = DisplayDpUtil.px2dp(px!!, context)

    Log.d("checked","[그룹생성] 선택한 크기 : ${px.dp}")
    Log.d("checked","[그룹생성] 선택한 크기 : $displayWidth")

    Scaffold(
        modifier = Modifier
            .windowInsetsPadding(
                WindowInsets.systemBars.only(
                    WindowInsetsSides.Vertical
                )
            ).imePadding(),
        topBar = {
            GroupToolBar(
                title = NavItem.Group.title,
                navClick = { (context as Activity).finish() }
            )
         },
        containerColor = "#F9F9F9".toColor(),
        bottomBar = {
            GroupBottomButtom(
                text = "저장",
                enabled = !loading,
                onClick = {

                    val selected = weeks.map {
                        isWeeks?.getValue(it) ?: false
                    }.toList()

                    if(title?.isEmpty() == true) {
                        isClosed.value = true
                        alertContext.value = "제목을 입력해주세요."
                        return@GroupBottomButtom
                    }

                    if(selected.all { !it }) {
                        isClosed.value = true
                        alertContext.value = "요일을 1개이상 선택해주세요."
                        return@GroupBottomButtom
                    }

                    Log.d("viewmodel:ID","[그룹생성] groupActivity ID : $viewModel")
                    loading = true
                    CoroutineScope(Dispatchers.Main).launch {
                        val groupId = GroupService.addGroupAlarm(
                            title = title,
                            hour = timePickerState?.hour ?: 7,
                            minute = timePickerState?.minute ?: 0,
                            alarmDate = selected,
                            members = members?.map { it.nickname }?.toList(),
                            soundName = soundName?.soundName,
                            soundVolume = soundVolume,
                            vibrate = vibration
                        )
                        if(groupId != null && groupId > 0) {
                            Log.d("response", "[그룹생성] response : $groupId")
                            suspend fun save() = coroutineScope {
                                async {
                                    saveAlarm(
                                        AlarmDto.toDto(
                                            Alarm(
                                                alarmId = groupId,
                                                title = title!!,
                                                hour = timePickerState?.hour!!,
                                                minute = timePickerState?.minute!!,
                                                alarmDate = selected,
                                                soundName = soundName?.soundName!!,
                                                soundVolume = soundVolume?.toInt()!!,
                                                vibrate = vibration!!,
                                                host = true
                                            )
                                        ), context
                                    )
                                }
                            }.await()
                            save()
                            if (groupId > 0) (context as Activity).finish()
                            loading = false
                        }
                    }
               }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(10.dp)
                    .verticalScroll(scrollerState),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GroupCard(
                    title = { GroupTitle(title = "그룹제목") },
                    content = { GroupSubjet(
                        title = title!!,
                        onChangeValue = { viewModel.onChangeTitle(it) },
                        interactionSource = interaction)
                    }
                )
                GroupCard(
                    title = { GroupTitle(title = "알람시간") },
                    content = {
                        WheelTimePicker(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(
                                    maxOf(200.dp)
                                ),
                            startTime = LocalTime.of(LocalTime.now().hour, LocalTime.now().minute),
                            minTime = LocalTime.of(0,0),
                            maxTime = LocalTime.MAX,
                            timeFormat = TimeFormat.AM_PM,
                            size = DpSize(displayWidth.dp,200.dp),
                            rowCount = 5,
                            textStyle = MaterialTheme.typography.titleLarge,
                            textColor = Color(0xFF000000),
                            selectorProperties = WheelPickerDefaults.selectorProperties(
                                enabled = true,
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFFFFFFF).copy(alpha = 0.2f),
                                border = BorderStroke(4.dp, Color(0xFFf1faee))
                            )
                        ) { snappedTime ->
                            viewModel.updateTimePicker(snappedTime.hour,snappedTime.minute)
                        }
//                        TimeInput(
//                            state = timePickerState!!,
//                            modifier = Modifier
//                                .padding(
//                                    start = 20.dp,
//                                    top = 10.dp,
//                                    bottom = 0.dp,
//                                    end = 0.dp
//                                )
//                                .focusable(true, interaction)
//                        )
                    }
                )
                GroupCard(
                    title = { GroupTitle(title = "요일선택") },
                    content = {
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val boxSize = this.maxWidth / 8
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 20.dp,
                                        top = 0.dp,
                                        bottom = 0.dp,
                                        end = 10.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                items(weeks) {item ->
                                    TextButton(
                                        modifier = Modifier.size(boxSize),
                                        onClick = {
                                            val change = !viewModel.getIsWeek(item)
                                            viewModel.onChangeWeek(item, change)
                                            Log.d("click event","[그룹생성] : $item value : ${viewModel.getIsWeek(item)}")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            contentColor = Color.Black,
                                            containerColor =
                                            viewModel.getIsWeek(item).let {
                                                if(it) {
                                                    "#00B4D8".toColor()
                                                } else {
                                                    MaterialTheme.colorScheme.background
                                                }
                                            }
                                        ),
                                        content = {
                                            Text( text = item )
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
                GroupInvite(
                    navController = navController,
                    members = members ?: mutableListOf()
                )
                GroupSound(
                    navController = navController,
                    sound = soundName?.soundName,
                )
                GroupTypeButton(
                    isVibrate = vibration ?: true,
                    viewModel = viewModel
                )
                GroupVolume(
                    volume = soundVolume ?: 7f,
                    setVolume = { viewModel.onChangeVolume(it) }
                )
            }
            if(isClosed.value) {
                CommonDialog(
                    title = "알림",
                    context = alertContext.value,
                    isClosed = isClosed,
                    isButton = false
                )
            }
        }
    )
    if(loading) {
        AnimationRotation()
    }
}