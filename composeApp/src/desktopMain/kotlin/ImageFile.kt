import com.ashampoo.kim.Kim
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.jvm.readMetadata
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.request.ImageRequest
import java.io.File

class ImageFile(
    val file: File,
) {
    val name: String = file.name

    val thumbnailRequest = ImageRequest(
        context = PlatformContext.INSTANCE,
        uri = file.absolutePath,
    ) {
        size(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
    }

    private var metadata: ImageMetadata? = null

    fun exifMetadataAsMap(): Map<String, Map<String, String>> {
        return metadata?.exif?.directories?.associate { directory ->
            directory.debugDescription to directory.entries.mapNotNull { entry ->
                entry.tagInfo?.name?.let { name ->
                    name to entry.valueDescription
                }
            }.toMap()
        } ?: emptyMap()
    }

    fun loadMetadata(file: File) {
        metadata = Kim.readMetadata(file)
    }

    companion object {
        const val THUMBNAIL_SIZE = 256
    }
}