package de.nilsauf.babyfone.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import de.nilsauf.babyfone.BabyfoneNavHost
import de.nilsauf.babyfone.models.listening.ListeningModel
import de.nilsauf.babyfone.models.streaming.StreamingModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BabyfoneMainLayout(
    streamingModel: StreamingModel = viewModel(key = StreamingModel.route),
    listeningModel: ListeningModel = viewModel(key = ListeningModel.route)
){
    val navController = rememberNavController()
    Scaffold(
        topBar = { BabyfoneTopBar(title = "Babyfone App") }
    ) {
        BabyfoneNavHost(
            navController,
            Modifier.padding(it),
            streamingModel,
            listeningModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BabyfoneTopBar(title: String){
    CenterAlignedTopAppBar(
        title = {
            Text(text = title, fontSize = 50.sp)
        }
    )
}