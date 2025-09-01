package com.salmanajmal.hsk4mastery.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.salmanajmal.hsk4mastery.ui.wordlist.WordListScreen

object Routes {
    const val WordList = "word_list"
    const val WordDetail = "word_detail/{wordId}"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.WordList) {
        composable(Routes.WordList) {
            WordListScreen(onWordClick = { id ->
                navController.navigate("word_detail/${id}")
            })
        }
        composable(
            Routes.WordDetail,
            arguments = listOf(navArgument("wordId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Placeholder screen; will be implemented later
            val wordId = backStackEntry.arguments?.getString("wordId") ?: ""
                com.salmanajmal.hsk4mastery.ui.worddetail.WordDetailScreen()
        }
    }
}
