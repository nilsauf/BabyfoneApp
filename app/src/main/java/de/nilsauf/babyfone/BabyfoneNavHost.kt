package de.nilsauf.babyfone

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun BabyfoneNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
){
    NavHost(navController = navController, startDestination = "start", modifier = modifier){
        composable(route = "start"){}
        composable(route = "setupStream"){}
        composable(route = "streaming"){}
        composable(route = "discovery"){}
        composable(route = "listening"){}
    }
}