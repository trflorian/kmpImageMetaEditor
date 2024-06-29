import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class MainViewModel: ViewModel() {
    private val _fileList = MutableStateFlow(emptyList<File>())
    val fileList = _fileList.asStateFlow()

    fun loadFiles(folder: File) {
        println("Loading files in folder: $folder")
        _fileList.value = folder.listFiles()?.toList()?.sorted() ?: emptyList()
        println("Loaded ${fileList.value.size} files")
    }
}