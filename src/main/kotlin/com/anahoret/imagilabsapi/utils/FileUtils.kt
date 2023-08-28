package com.anahoret.imagilabsapi.utils

import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText

object FileUtils {

    fun readDirectoryFiles(path: String): List<FileRead> {
        val files = mutableListOf<FileRead>()
        val directory = Paths.get(path)

        return if (Files.exists(directory) && Files.isDirectory(directory)) {

            Files.newDirectoryStream(directory).use { directoryStream ->
                for (file in directoryStream) {
                    if (Files.isRegularFile(file)) {
                        val name = file.nameWithoutExtension
                        val sourceCode = file.readText()
                        files.add(FileRead(name, sourceCode))
                    }
                }
            }

            files
        } else {
            emptyList()
        }
    }
}


class FileRead(
    val fileName: String,
    val text: String
)
