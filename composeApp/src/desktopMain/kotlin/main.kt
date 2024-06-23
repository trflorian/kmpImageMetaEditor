import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.painterResource

import kmpimagemetaeditor.composeapp.generated.resources.Res
import kmpimagemetaeditor.composeapp.generated.resources.icon
import ui.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Image Metadata Editor",
        icon = painterResource(Res.drawable.icon)
    ) {
        App()
    }
}