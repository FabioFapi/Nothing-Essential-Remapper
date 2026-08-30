package rix.fapi.nothingessentialremapper.ui.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import rix.fapi.nothingessentialremapper.data.KeyMappingRepository
import rix.fapi.nothingessentialremapper.gesture.GestureType
import rix.fapi.nothingessentialremapper.ui.home.HomeScreen
import rix.fapi.nothingessentialremapper.ui.mapping.AppPickerScreen
import rix.fapi.nothingessentialremapper.ui.mapping.MappingScreen
import rix.fapi.nothingessentialremapper.ui.setup.SetupScreen

private object Routes {
    const val HOME = "home"
    const val SETUP = "setup"
    const val MAPPING = "mapping"
    const val APP_PICKER = "app_picker/{gesture}"
    const val APP_PICKER_ARG = "gesture"
    fun appPicker(gesture: GestureType) = "app_picker/${gesture.name}"
}

@Composable
fun AppNavHost(
    repository: KeyMappingRepository,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
    ) {
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
                onPickApp = { gesture -> navController.navigate(Routes.appPicker(gesture)) }
            )
        }
        composable(
            route = Routes.APP_PICKER,
            arguments = listOf(navArgument(Routes.APP_PICKER_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val gestureName = backStackEntry.arguments?.getString(Routes.APP_PICKER_ARG)
            val gesture = gestureName?.let { runCatching { GestureType.valueOf(it) }.getOrNull() }
                ?: GestureType.SINGLE_PRESS
            AppPickerScreen(
                repository = repository,
                gesture = gesture,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
