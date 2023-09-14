package com.appa.snoop.presentation.ui.main

import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.appa.snoop.presentation.common.topbar.SharedTopAppBar
import com.appa.snoop.presentation.common.topbar.utils.rememberAppBarState
import com.appa.snoop.presentation.navigation.MainNav
import com.appa.snoop.presentation.navigation.SnoopNavHost

@Composable
fun MainScreen(
    type: String? = null,
    articleId: String? = null
) {
    val navController = rememberNavController()
    val appBarState = rememberAppBarState(navController = navController)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            if (appBarState.isVisible) {
                SharedTopAppBar(appBarState = appBarState)
            }
        },
        bottomBar = {
            if (MainNav.isMainRoute(currentRoute)) {
                CustomTabBar(navController = navController, currentRoute = currentRoute)
            }
        },
    ) {
        SnoopNavHost(
            innerPaddings = it,
            navController = navController,
//            showSnackBar = { message ->
//                scope.launch {
//                    snackBarHostState.showSnackbar(message)
//                }
//            }
        )
        LaunchedEffect(Unit) {
            if (type != null && articleId != null) {
                Log.d("TEST", "MainScreen: PendingIntent 호출")
//                navController.navigate("${BoardDetailNav.route}/${type.lowercase(Locale.getDefault())}/$articleId")
            }
        }
    }
}