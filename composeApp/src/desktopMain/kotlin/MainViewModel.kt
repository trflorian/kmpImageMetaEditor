import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ashampoo.kim.Kim
import com.ashampoo.kim.format.jpeg.JpegRewriter
import com.ashampoo.kim.input.JvmInputStreamByteReader
import com.ashampoo.kim.input.use
import com.ashampoo.kim.jvm.readMetadata
import com.ashampoo.kim.output.OutputStreamByteWriter
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(
    initialFolder: File? = null
) : ViewModel() {
    private val _currentFolder = MutableStateFlow(initialFolder)
    val currentFolder = _currentFolder.asStateFlow()

    private val _fileList = MutableStateFlow(emptyList<File>())
    val fileList = _fileList.asStateFlow()

    val imageRequests = fileList.map {
        it.map { file ->
            ImageRequest(
                context = PlatformContext.INSTANCE,
                uri = file.absolutePath,
            ) {
                size(256, 256)
            }
        }
    }

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
                "jpg",
                "jpeg",
                "png",
                "gif",
                "bmp",
                "webp"
            )
        }
        _fileList.value = imagesInFolder.sortedBy { it.name }
        println("Loaded ${fileList.value.size} files")
    }

    fun loadMetadata(file: File) = viewModelScope.launch(
        Dispatchers.IO
    ) {
        // create modified file with name suffix _modified
        val modifiedFile =
            File(file.parent, "${file.nameWithoutExtension}_modified.${file.extension}")

        // create file
        if (!modifiedFile.exists()) {
            modifiedFile.createNewFile()
        }

        val metadata = Kim.readMetadata(file)

        // get xmp metadata
        val xmpMetadata = metadata?.xmp
        println("XMP Metadata: $xmpMetadata")

        OutputStreamByteWriter(modifiedFile.outputStream()).use { writer ->
            JpegRewriter.updateXmpXml(
                JvmInputStreamByteReader(file.inputStream(), file.length()),
                byteWriter = writer,
                xmpXml = """<?xpacket begin="﻿" id="W5M0MpCehiHzreSzNTczkc9d"?>
<x:xmpmeta
	xmlns:x="adobe:ns:meta/">
	<rdf:RDF
		xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
		<rdf:Description rdf:about="DJI Meta Data"
			xmlns:tiff="http://ns.adobe.com/tiff/1.0/"
			xmlns:exif="http://ns.adobe.com/exif/1.0/"
			xmlns:xmp="http://ns.adobe.com/xap/1.0/"
			xmlns:xmpMM="http://ns.adobe.com/xap/1.0/mm/"
			xmlns:dc="http://purl.org/dc/elements/1.1/"
			xmlns:crs="http://ns.adobe.com/camera-raw-settings/1.0/"
			xmlns:drone-dji="http://www.dji.com/drone-dji/1.0/"
			xmlns:GPano="http://ns.google.com/photos/1.0/panorama/"
   xmp:ModifyDate="2024-05-10 14:09:12"
   xmp:CreateDate="2024-05-10 14:09:12"
   tiff:Make="Hasselblad"
   tiff:Model="L2D-20c"
   dc:format="image/jpeg"
   drone-dji:Version="1.2"
   drone-dji:GpsStatus="Normal"
   drone-dji:AltitudeType="GpsFusionAlt"
   drone-dji:GpsLatitude="+46.592612704"
   drone-dji:GpsLongitude="+7.525073398"
   drone-dji:AbsoluteAltitude="+1538.962"
   drone-dji:RelativeAltitude="+26.600"
   drone-dji:GimbalRollDegree="+0.00"
   drone-dji:GimbalYawDegree="-131.70"
   drone-dji:GimbalPitchDegree="-35.40"
   drone-dji:FlightRollDegree="+1.30"
   drone-dji:FlightYawDegree="-132.20"
   drone-dji:FlightPitchDegree="+4.30"
   drone-dji:FlightXSpeed="0.0"
   drone-dji:FlightYSpeed="0.0"
   drone-dji:FlightZSpeed="0.0"
   drone-dji:CamReverse="0"
   drone-dji:GimbalReverse="0"
   drone-dji:SensorTemperature="31.0"
   drone-dji:ProductName="DJIMavic3Classic"
   drone-dji:SelfData="HELLO WORLD"
   crs:Version="7.0"
   crs:HasSettings="False"
   crs:HasCrop="False"
   crs:AlreadyApplied="False">
		</rdf:Description>
	</rdf:RDF>
</x:xmpmeta>
<?xpacket end="w"?>"""
            )
        }
    }
}