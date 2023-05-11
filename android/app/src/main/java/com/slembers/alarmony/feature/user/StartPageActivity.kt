package com.slembers.alarmony.feature.user


//import com.slembers.alarmony.feature.user.Navigation


//통신api
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.slembers.alarmony.R
import com.slembers.alarmony.feature.common.NavItem
import com.slembers.alarmony.network.repository.MemberService.login
import com.slembers.alarmony.network.repository.MemberService.putRegistTokenAfterSignIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview


enum class Routes() {
    Signup,
    Setting
}


class StartPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{

            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            )  {

//                SignupScreen()
//                Findpswd()
//                FindId()()
//                ProfileSetting()
//                AccountMtnc()

//                LoginScreen()
//                Navigation()

            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalComposeUiApi::class)
//위 @OptIn(ExperimentalGlideComposeApi::class)이 회색으로 나오는 이유는
//사용되지 않아서가 아니라 실험적이고 불안정한 기능이기 때문이다.
@Composable
@ExperimentalMaterial3Api
@Preview(showBackground = true)
fun LoginScreen(navController: NavController = rememberNavController()) {
//    val checkedState = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    // 아이디와 비밀번호에 대한 상태를 저장할 mutableState 변수 선언
    val idState = remember { mutableStateOf("") }
    var idError = rememberSaveable  { mutableStateOf<Boolean>(false) }
    val passwordState = remember { mutableStateOf("") }
    var passwordVisibility = true
    var isSuccess = false
    var msg = ""

    val usernameRegex = "^[a-z0-9]{4,20}$".toRegex()
    val passwordRegex = "^[a-zA-Z\\d]{8,16}\$".toRegex()

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {

        mascott(drawing = R.drawable.mascot_foreground)
        logo(drawing = R.drawable.alarmony)
        OutlinedTextField(
            value = idState.value,
            onValueChange = { idState.value = it;

                idError = mutableStateOf(!usernameRegex.matches(it))
                            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Black,
                unfocusedBorderColor = Black,
                errorBorderColor = Red
                ),
            label = { Text("아이디") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .fillMaxWidth(),

            isError = idError.value,



        )

        OutlinedTextField(
            value = passwordState.value,
            onValueChange = { passwordState.value = it },
            label = { Text("비밀번호") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),

            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Black,
                unfocusedBorderColor = Black,
                errorBorderColor= Red,
            ),
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .fillMaxWidth(),


            visualTransformation =  PasswordVisualTransformation(),


        )
//아래는 자동로그인 체크박스

//        Row(modifier = Modifier.padding(0.dp)) {
//            // Checkbox Composable을 사용하여 체크박스 UI를 생성
//            Checkbox(
//                checked = checkedState.value,
//                onCheckedChange = {
//                    checkedState.value = it
//                    if (it) {
////                        prefs.setString("autoLogin", "true")
////                        prefs.setBoolean("auto_login", true)
//                      Log.d("체크박스", "자동 로그인 온")
//                      Log.d("체크박스", "${prefs.getBoolean("auto_login", false)}")
//                    } else {
////                        prefs.setBoolean("auto_login", false)
//                        Log.d("체크박스", "자동 로그인 오프")
//                        Log.d("체크박스", "${prefs.getBoolean("auto_login", false)}")
//
//                    }
//                }
//            )
//            Text(text = "자동 로그인 ")
//        }



//        아래는 로그인을 위한 통신로직을 RetrofitClient에서 가져와서 수행

        Button(
//MutableState<String>와 String은 형식이 다르기에 String 값을 보내기 위해 .value를 붙여준다.
            onClick = {
                Log.d("확인", "${idState.value}, ${passwordState.value} +로그인")
                CoroutineScope(Dispatchers.Main).launch {
                    val result = login(
                        username = idState.value,
                        password = passwordState.value
                    )
                    putRegistTokenAfterSignIn()
                    Log.d("INFO","result : $result")
                    if(result) navController.navigate(NavItem.AlarmListScreen.route)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 15.dp)
                .clip(RoundedCornerShape(20.dp)),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Black, // Set the background color of the button
                contentColor = Color.White // Set the text color of the button
            )

        ) {
            Text("로그인")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(onClick = {
                navController.navigate(NavItem.Signup.route)

            },
                modifier = Modifier.size(width = 120.dp, height = 50.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Black // Set the font color of the button
                )) {
                Text(text = "회원가입")

            }
            TextButton(onClick = {
                navController.navigate(NavItem.FindIdActivity.route)
            },

                modifier = Modifier.size(width = 120.dp, height = 50.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Black // Set the font color of the button
                )

            ) {
                Text(text = "아이디 찾기")
            }

            TextButton(onClick = {
                navController.navigate(NavItem.FindPswdActivity.route)
            } ,
                modifier = Modifier.size(width = 120.dp, height = 50.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Black // Set the font color of the button
                )

            ) {
                Text(text = "비밀번호 찾기")
            }
        }
    }


}

@Composable
fun mascott(drawing:Int) {
    Image(
        painter = painterResource(drawing),
        contentDescription = "mascott image",
        modifier = Modifier
            .padding(top = 130.dp , bottom = 20.dp)
    )

}

@Composable
fun logo(drawing:Int) {
    Image(painter = painterResource(id = R.drawable.alarmony),
        contentDescription = "mascott image",
                modifier = Modifier
                    .padding( bottom = 20.dp)
    )

}
