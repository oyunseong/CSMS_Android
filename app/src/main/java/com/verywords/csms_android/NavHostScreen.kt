package com.verywords.csms_android

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.verywords.Navigation
import com.verywords.csms_android.ui.screen.home.HomeScreen
import com.verywords.csms_android.ui.screen.room.DatabaseScreen

// TODO 네비게이션 기능 필요시 추가 예정.
@Composable
fun NavHostScreen(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    fun clearAndNavigate(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            popUpTo(0) { inclusive = true }
        }
    }

    fun popUp() {
        navController.popBackStack()
    }

    fun navigate(route: String) {
        navController.navigate(route) { launchSingleTop = true }
    }

    NavHost(
        navController = navController,
        startDestination = Navigation.Routes.HomeScreen
    ) {
        composable(
            route = Navigation.Routes.HomeScreen
        ) {
            HomeScreen(
                modifier = modifier,
                navigate = ::navigate
            )
        }

        composable(
            route = Navigation.Routes.DatabaseScreen
        ) {
            DatabaseScreen(
                modifier = modifier,
            )
        }
    }
}