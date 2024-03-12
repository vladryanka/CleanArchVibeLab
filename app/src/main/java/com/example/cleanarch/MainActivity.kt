package com.example.cleanarch

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cleanarch.data.Fact
import com.example.cleanarch.data.View
import com.example.cleanarch.data.arrayFacts
import com.example.cleanarch.data.link
import com.google.gson.Gson

class MainActivity : ComponentActivity() {
    private val viewModel: View by viewModels()
    private lateinit var receiver: BroadcastReceiver
    private lateinit var navController: NavHostController

    //private val workRequest = OneTimeWorkRequest.Builder(CatWorker::class.java).build()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            navController = rememberNavController()
            MainScreen(navController, viewModel)
        }
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val catFacts = Gson().fromJson(
                    intent?.getStringExtra("catFacts"),
                    Array<Fact>::class.java
                ).toList()
                viewModel.updateCatFacts(catFacts)
                arrayFacts.clear()
                for (it in catFacts) {
                    arrayFacts.add(it.fact)
                }
                navController.navigate("s2")
            }
        }
    }
}

@Composable
fun MainScreen(navController: NavHostController, viewModel: View) {
    NavHost(navController = navController, startDestination = "s1") {
        composable("s1") {
            FirstScreen(viewModel = viewModel, navController = navController)
        }
        composable("s2") {
            ApiInfo(navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FirstScreen(viewModel: View, navController :NavController) {
    val context = LocalContext.current

    val catFacts by viewModel.catFacts.observeAsState(initial = emptyList())
    val textState = remember { mutableStateOf(TextFieldValue()) }
    Scaffold(
        topBar = @Composable {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(stringResource(R.string.facts), color = Color.White)
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                BottomBar(navController)
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxHeight(0.5f)) {
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = it
                ) {
                    itemsIndexed(
                        catFacts
                    ) { _, item ->
                        FactItem(
                            fact = item.fact,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.5f)
                    .align(Alignment.CenterHorizontally)
            ) {
                TextField(
                    value = textState.value, onValueChange = { newValue ->
                        textState.value = newValue
                    },
                    label = { Text(text = "Count", color = Color.Black) },
                    modifier = Modifier.padding(8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)

                )

                Button(
                    onClick = {
                        var id = 0
                        for (index in link.indices) {
                            if (link[index] == '=') {
                                id = index
                            }
                        }
                        link = link.substring(0, id+1) + textState.value.text
                        viewModel.service(context)
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "Reload")
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ApiInfo(navController: NavController) {
    Scaffold(
        topBar = @Composable {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(stringResource(R.string.api_info), color = Color.White)
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                BottomBar(navController)
            }

        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = link,
                color = Color.Black,
                modifier = Modifier.padding(32.dp)
            )
        }
    }

}

@Composable
fun FactItem(
    fact: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Text(
            text = fact,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BottomBar(navController: NavController){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f).clickable {
                navController.navigate("s1")
            }
        ) {
            Image(
                painter = painterResource(R.drawable.crown),
                contentDescription = stringResource(R.string.facts),
                modifier = Modifier.size(24.dp)
            )
            Text(stringResource(R.string.facts), modifier = Modifier.padding(8.dp))
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f).clickable {
                navController.navigate("s2")
            }
        ) {
            Image(
                painter = painterResource(R.drawable.web),
                contentDescription = stringResource(R.string.web),
                modifier = Modifier.size(24.dp)
            )
            Text(stringResource(R.string.web), modifier = Modifier.padding(8.dp))
        }
    }
}