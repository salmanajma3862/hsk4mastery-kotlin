package com.salmanajmal.hsk4mastery.ui.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
        Routes.WordList,
        Routes.ReviewDashboard,
        Routes.Practice,
        Routes.Progress,
    )
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val showBottomBar = when (currentRoute) {
                Routes.WordList, Routes.ReviewDashboard, Routes.Practice, Routes.Progress -> true
                // Hide on detail and active review screens
                Routes.WordDetail, Routes.ActiveReview -> false
                else -> false
            }
            if (showBottomBar) {
                Column {
                    Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                    NavigationBar(
                        modifier = Modifier.height(120.dp),
                        containerColor = Color(0xFFF9FAFB),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        items.forEach { route ->
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
                                icon = {
                                    when (route) {
                                        Routes.ReviewDashboard -> Icon(imageVector = Icons.Default.Repeat, contentDescription = "Review", modifier = Modifier.height(22.dp))
                                        Routes.Practice -> Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Practice", modifier = Modifier.height(22.dp))
                                        Routes.Progress -> Icon(imageVector = Icons.Default.TrendingUp, contentDescription = "Progress", modifier = Modifier.height(22.dp))
                                        else -> Icon(imageVector = Icons.Default.List, contentDescription = "Words", modifier = Modifier.height(22.dp))
                                    }
                                },
                                alwaysShowLabel = false,
                                label = null
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        // Do not apply outer Scaffold innerPadding to NavHost directly to avoid double-padding
        // Individual screens should manage their own content padding (e.g. WordList adds bottom padding)
        NavHost(
            navController = navController,
            startDestination = Routes.WordList,
            modifier = Modifier
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
                com.salmanajmal.hsk4mastery.ui.worddetail.WordDetailScreen(navController)
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
