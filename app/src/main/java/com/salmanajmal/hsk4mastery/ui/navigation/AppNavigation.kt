package com.salmanajmal.hsk4mastery.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.salmanajmal.hsk4mastery.ui.review.ReviewDashboardScreen
import com.salmanajmal.hsk4mastery.ui.wordlist.WordListScreen

object Routes {
    const val WordList = "word_list"
    const val WordDetail = "word_detail/{wordId}"
    const val ReviewDashboard = "review_dashboard"
    const val ActiveReview = "active_review/{wordIds}"
    const val Practice = "practice"
    const val Progress = "progress"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val items = listOf(
        Routes.WordList to "Words",
        Routes.ReviewDashboard to "Review",
        Routes.Practice to "Practice",
        Routes.Progress to "Progress",
    )
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            NavigationBar {
                items.forEach { (route, label) ->
                    val selected = currentRoute == route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(
                            when (route) {
                                Routes.ReviewDashboard -> "🔁"
                                Routes.Practice -> "🧩"
                                Routes.Progress -> "📈"
                                else -> "📚"
                            }
                        ) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.WordList,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.WordList) {
                WordListScreen(onWordClick = { id ->
                    navController.navigate("word_detail/${id}")
                })
            }
            composable(
                Routes.WordDetail,
                arguments = listOf(navArgument("wordId") { type = NavType.StringType })
            ) {
                com.salmanajmal.hsk4mastery.ui.worddetail.WordDetailScreen()
            }
            composable(
                route = Routes.ActiveReview,
                arguments = listOf(navArgument("wordIds") { type = NavType.StringType })
            ) {
                com.salmanajmal.hsk4mastery.ui.review.ActiveReviewScreen(navController)
            }
            composable(Routes.ReviewDashboard) {
                com.salmanajmal.hsk4mastery.ui.review.ReviewDashboardScreenForNav(navController)
            }
            composable(Routes.Practice) {
                com.salmanajmal.hsk4mastery.ui.practice.PracticeScreen()
            }
            composable(Routes.Progress) {
                com.salmanajmal.hsk4mastery.ui.progress.ProgressScreen()
            }
        }
    }
}
