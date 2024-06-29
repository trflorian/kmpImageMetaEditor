import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(
    initialFolder: File? = null
) : ViewModel() {
    private val _currentFolder = MutableStateFlow(initialFolder)
    val currentFolder = _currentFolder.asStateFlow()

    private val _imageFiles = MutableStateFlow(emptyList<ImageFile>())
    val imageFiles = _imageFiles.asStateFlow()

    private val _selectedFile = MutableStateFlow<ImageFile?>(null)
    val selectedFile = _selectedFile.asStateFlow()

    init {
        if (initialFolder != null) {
            loadFiles(initialFolder)
        }
    }

    fun loadFiles(folder: File) {
        println("Loading files in folder: $folder")
        val allFilesInFolder = folder.listFiles()?.toList() ?: emptyList()
        val imagesInFolder = allFilesInFolder.filter {
            it.extension.lowercase() in listOf(
                "jpg", "jpeg", "png", "gif", "bmp", "webp"
            )
        }
        _imageFiles.value = imagesInFolder.map { ImageFile(it) }.sortedBy { it.name }
        println("Loaded ${imageFiles.value.size} files")
    }

    fun selectFile(file: ImageFile) {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            file.loadMetadata(file.file)
            _selectedFile.value = file
        }
    }
}