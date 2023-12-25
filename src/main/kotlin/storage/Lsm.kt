package org.example.storage

class Lsm(private val flushThreshold: Int, private val dataPath: String): AutoCloseable {
    private var memTable = MemTable()
    private val ssTable = SSTable(dataPath)
    private fun flush() {
        if(!memTable.isEmpty()) {
            ssTable.write(memTable.table)
            memTable.clear()
        }
    }
    fun set(key: String, value: String) {
        memTable.set(key, value)
        if(memTable.table.size >= flushThreshold) {
            flush()
        }
    }
    fun get(key: String): String? {
        val fromMemory = memTable.get(key)
        if(fromMemory != null) {
            return fromMemory
        }
        val fromDisk = ssTable.get(key)
        return fromDisk
    }

    override fun close() {
        flush()
    }
}