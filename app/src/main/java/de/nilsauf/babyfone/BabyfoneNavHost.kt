package de.nilsauf.babyfone

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.nilsauf.babyfone.models.listening.ListeningModel
import de.nilsauf.babyfone.models.streaming.StreamingModel
import de.nilsauf.babyfone.ui.listening.ListeningScreen
import de.nilsauf.babyfone.ui.start.StartScreen
import de.nilsauf.babyfone.ui.start.startRoute
import de.nilsauf.babyfone.ui.streaming.StreamingScreen

@Composable
fun BabyfoneNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    streamingModel: StreamingModel,
    listeningModel: ListeningModel
){
    NavHost(navController = navController, startDestination = startRoute, modifier = modifier){
        composable(route = startRoute){
            streamingModel.stopStream()
            listeningModel.stopStream()
            StartScreen(
                onClickSetupStream = { navController.navigate(StreamingModel.route) },
                onClickStartListening = { navController.navigate(ListeningModel.route) }
            )
        }
        composable(route = StreamingModel.route){
            StreamingScreen(streamingModel)
        }
        composable(route = ListeningModel.route){
            ListeningScreen(listeningModel)
        }
    }
}