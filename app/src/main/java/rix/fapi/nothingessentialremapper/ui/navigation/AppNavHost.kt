package rix.fapi.nothingessentialremapper.ui.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import rix.fapi.nothingessentialremapper.data.KeyMappingRepository
import rix.fapi.nothingessentialremapper.gesture.GestureType
import rix.fapi.nothingessentialremapper.ui.home.HomeScreen
import rix.fapi.nothingessentialremapper.ui.mapping.ActionPickerScreen
import rix.fapi.nothingessentialremapper.ui.mapping.AppPickerScreen
import rix.fapi.nothingessentialremapper.ui.mapping.MappingScreen
import rix.fapi.nothingessentialremapper.ui.onboarding.OnboardingScreen
import rix.fapi.nothingessentialremapper.ui.setup.SetupScreen

private object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val SETUP = "setup"
    const val MAPPING = "mapping"
    const val ACTION_PICKER = "action_picker/{gesture}"
    const val GESTURE_ARG = "gesture"
    const val APP_PICKER = "app_picker/{gesture}"
    fun actionPicker(gesture: GestureType) = "action_picker/${gesture.name}"
    fun appPicker(gesture: GestureType) = "app_picker/${gesture.name}"
}

private fun gestureArgOf(argument: String?): GestureType =
    argument?.let { runCatching { GestureType.valueOf(it) }.getOrNull() } ?: GestureType.SINGLE_PRESS

@Composable
fun AppNavHost(
    repository: KeyMappingRepository,
    navController: NavHostController = rememberNavController()
) {
    val onboardingCompleted by repository.isOnboardingCompleted.collectAsState(initial = null)
    val startDestination = when (onboardingCompleted) {
        true -> Routes.HOME
        false -> Routes.ONBOARDING
        null -> return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
    ) {
        composable(Routes.ONBOARDING) {
            val scope = rememberCoroutineScope()
            OnboardingScreen(
                onGetStarted = {
                    scope.launch { repository.setOnboardingCompleted(true) }
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                repository = repository,
                onOpenSetup = { navController.navigate(Routes.SETUP) },
                onOpenMapping = { navController.navigate(Routes.MAPPING) }
            )
        }
        composable(Routes.SETUP) {
            SetupScreen(repository = repository, onBack = { navController.popBackStack() })
        }
        composable(Routes.MAPPING) {
            MappingScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onOpenActionPicker = { gesture -> navController.navigate(Routes.actionPicker(gesture)) }
            )
        }
        composable(
            route = Routes.ACTION_PICKER,
            arguments = listOf(navArgument(Routes.GESTURE_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val gesture = gestureArgOf(backStackEntry.arguments?.getString(Routes.GESTURE_ARG))
            ActionPickerScreen(
                repository = repository,
                gesture = gesture,
                onBack = { navController.popBackStack() },
                onPickApp = { navController.navigate(Routes.appPicker(gesture)) }
            )
        }
        composable(
            route = Routes.APP_PICKER,
            arguments = listOf(navArgument(Routes.GESTURE_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val gesture = gestureArgOf(backStackEntry.arguments?.getString(Routes.GESTURE_ARG))
            AppPickerScreen(
                repository = repository,
                gesture = gesture,
                onBack = { navController.popBackStack(Routes.MAPPING, inclusive = false) }
            )
        }
    }
}
