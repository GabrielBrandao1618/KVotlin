package org.example.storage

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.RandomAccessFile
import java.util.Date
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.exists

class SSTable(private val dataPath: String) {
    fun write(data: HashMap<String, String>) {
        val index = HashMap<String, Long>()
        val currDate = Date()
        val time = currDate.time
        val fullFilePath = "$dataPath/$time.txt"
        ensureDataFileExists(fullFilePath)
        var rawData = ""

        var currentOffset: Long = 0
        for ((k, v) in data) {
            index[k] = currentOffset
            rawData += "$v\n"
            // The + 1 represents the \n character
            currentOffset += v.length + 1
        }
        writeFileData(fullFilePath, rawData, index)
    }
    private fun ensureDataFileExists(fullPath: String) {
        val parsedFullFilePath = Path(fullPath)
        if(!parsedFullFilePath.exists()) {
            parsedFullFilePath.createFile()
        }
    }
    private fun writeFileData(path: String, data: String, index: HashMap<String, Long>) {
        val w = BufferedWriter(FileWriter(path))
        var strIndex = ""
        for((k, v) in index) {
            strIndex += "$k.$v\n"
        }
        val indexEndOffset = strIndex.length + 5 // The +5 represents the first 4 bytes plus the \n
        val indexEndOffsetHex = String.format("%04x", indexEndOffset)
        w.appendLine(indexEndOffsetHex)

        w.append(strIndex)
        w.append(data)

        w.close()
    }
    private fun getIndexEndOffsetFromFile(filePath: String): Long {
        val r = BufferedReader(FileReader(filePath))
        val offset = r.readLine().toLong(16)
        r.close()
        return offset
    }
    private fun getIndexFromFile(filePath: String): HashMap<String, Long> {
        val r = BufferedReader(FileReader(filePath))
        val hexIndexEndOffset = r.readLine()
        val intIndexEndOffset = hexIndexEndOffset.toInt(16)
        val indexChars = CharArray(intIndexEndOffset - 5)
        r.read(indexChars)
        r.close()

        val rawIndex = indexChars.joinToString("")
        val parsedIndex = parseIndex(rawIndex)
        return parsedIndex

    }
    private fun parseIndex(rawIndex: String): HashMap<String, Long> {
        val index = HashMap<String, Long>()
        for(line in rawIndex.split("\n")) {
            val fields = line.split(".", limit = 2)
            if(fields.size == 2) {
                val key = fields[0]
                val value = fields[1]
                index[key] = value.toLong()
            }
        }
        return index
    }
    private fun readWithOffset(filePath: String, offset: Long): String {
        val indexEndOffset = getIndexEndOffsetFromFile(filePath)
        val f = RandomAccessFile(filePath, "r")

        f.seek(indexEndOffset + offset)
        val line = f.readLine()
        f.close()
        return line
    }
    private fun readFromFile(filePath: String, key: String): String? {
        val fileIndex = getIndexFromFile(filePath)
        val dataOffset = fileIndex[key] ?: return null
        val value = readWithOffset(filePath, dataOffset)
        return value
    }
    fun get(key: String): String? {
        val dataFiles = getAllDataFiles()
        for(file in dataFiles) {
            val fromFile = readFromFile(file.path, key)
            if(fromFile != null) {
                return fromFile
            }
        }
        return null
    }
    private fun getAllDataFiles(): Array<File> {
        val dir = File(dataPath)

        if(dir.isDirectory()) {
            return dir.listFiles() ?: arrayOf()
        }
        return arrayOf()
    }

}