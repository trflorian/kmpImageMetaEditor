package ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ashampoo.kim.Kim
import com.ashampoo.kim.format.jpeg.JpegRewriter
import com.ashampoo.kim.input.JvmInputStreamByteReader
import com.ashampoo.kim.input.use
import com.ashampoo.kim.jvm.readMetadata
import com.ashampoo.kim.model.MetadataUpdate
import com.ashampoo.kim.output.OutputStreamByteWriter
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
            FilesView()
        }
    }
}

@Composable
private fun FilesView() {
    // load files initially into a variable
    val files by
        remember { mutableStateOf(loadFilesInFolder(File("/home/florian/Pictures/240511_Bodenfluh/"))) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(files) { file ->
            Text(file.name, modifier = Modifier.clickable(onClick = {
                println("Clicked on file: ${file.name}")

                // load metadata of that image file
                loadMetadata(file)
            }))
        }
    }
}

private fun loadFilesInFolder(folder: File): List<File> {
    return folder.listFiles()?.toList()?.sorted() ?: emptyList()
}

private fun loadMetadata(file: File) {
    // create modified file with name suffix _modified
    val modifiedFile = File(file.parent, "${file.nameWithoutExtension}_modified.${file.extension}")

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