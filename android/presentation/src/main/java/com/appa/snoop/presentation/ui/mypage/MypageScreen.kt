package com.appa.snoop.presentation.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.appa.snoop.presentation.ui.theme.DarkGrayColor
import com.appa.snoop.presentation.util.effects.MainLaunchedEffect

@Composable
fun MypageScreen(
    navController: NavController,
) {
    MainLaunchedEffect(navController)
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "mypage",
            fontSize = 30.sp
        )

        // 테스트 코드
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkGrayColor)
        )
    }
}