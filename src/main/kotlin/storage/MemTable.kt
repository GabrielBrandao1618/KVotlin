package org.example.storage

class MemTable {
    val table = HashMap<String, String>()
    fun set(key: String, value: String) {
        table[key] = value
    }
    fun get(key: String): String? {
        return table[key]
    }
    fun clear() {
        table.clear()
    }
    fun isEmpty(): Boolean {
        return table.isEmpty()
    }

}