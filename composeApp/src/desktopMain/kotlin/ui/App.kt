package ui

import MainViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.panpf.sketch.AsyncImage
import org.jetbrains.compose.ui.tooling.preview.Preview
import ui.theme.AppTheme
import java.io.File

@Composable
@Preview
fun App() {
    AppTheme(
        /*
        * On Linux checking the system theme is not supported yet, we have to specify the theme manually.
        * https://github.com/JetBrains/compose-multiplatform/issues/169
         */
        darkTheme = true,
    ) {
        Surface(
            Modifier.fillMaxSize()
        ) {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = "main",
            ) {
                composable("main") {
                    MainView()
                }
            }
        }
    }
}

@Composable
private fun MainView() {
    // create three views side by side, expand them equally to fill the screen
    val viewModel = viewModel<MainViewModel> {
        MainViewModel(
            initialFolder = File("/home/florian/Pictures/sample_images/")
        )
    }
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            SearchBar(viewModel)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(8.dp)
            ) {
                FilesView(viewModel)
            }
            Column(
                modifier = Modifier.weight(1f).padding(8.dp),
            ) {
                ImagesView(viewModel)
            }
            Column(
                modifier = Modifier.weight(1f).padding(8.dp),
            ) {
                MetadataView(viewModel)
            }
        }
    }
}

@Composable
fun SearchBar(viewModel: MainViewModel) {
    var pathInput by remember { mutableStateOf(viewModel.currentFolder.value?.absolutePath ?: "") }
    val focusManager = LocalFocusManager.current
    OutlinedTextField(value = pathInput,
        onValueChange = { pathInput = it },
        label = { Text("Path") },
        singleLine = true,
        leadingIcon = {
            IconButton(onClick = {
                viewModel.loadFiles(File(pathInput))
                focusManager.clearFocus()
            }) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
            }
        },
        trailingIcon = {
            IconButton(onClick = {
                pathInput = ""
            }) {
                if (pathInput.isNotBlank()) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth().onKeyEvent { keyEvent ->
            if (keyEvent.key != Key.Enter) return@onKeyEvent false
            if (keyEvent.type == KeyEventType.KeyUp) {
                viewModel.loadFiles(File(pathInput))
                focusManager.clearFocus()
            }
            return@onKeyEvent true
        })
}

@Composable
fun ImagesView(viewModel: MainViewModel) {
    val files by viewModel.fileList.collectAsState()
    val imageRequests by viewModel.imageRequests.collectAsState(emptyList())
    // create a grid of all images
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 64.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(imageRequests) { request ->
            AsyncImage(
                request = request,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
            )
        }
    }
}

@Composable
fun MetadataView(viewModel: MainViewModel) {
    val files by viewModel.fileList.collectAsState()
    Text("Metadata View ${files.size}")
}

@Composable
private fun FilesView(viewModel: MainViewModel) {
    val files by viewModel.fileList.collectAsState()
    // load files initially into a variable
    LazyColumn {
        items(files) { file ->
            Row(modifier = Modifier.fillMaxWidth().clickable {
                println("Clicked on file: ${file.name}")

                viewModel.loadMetadata(file)
            }) {
                Text(
                    file.name, modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}