package org.example

import org.example.storage.Lsm

fun main() {
    val lsm = Lsm(1024, ".data")
//    lsm.set("name", "Gabriel")
//    lsm.set("game", "CoC")
//    lsm.set("fame", "High")
//    lsm.set("shame", "None")

    val fromDisk = lsm.get("name")
    println(fromDisk)

    lsm.close()
}