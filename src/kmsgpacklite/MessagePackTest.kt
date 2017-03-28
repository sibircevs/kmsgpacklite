package kmsgpacklite

//>kotlinc kmsgpacklite/Converter.kt kmsgpacklite/Value.kt kmsgpacklite/MessagePack.kt kmsgpacklite/MessagePackTest.kt
//>kotlin -classpath kmsgpacklite/MessagePackTestKt.class kmsgpacklite.MessagePackTestKt

import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream

fun main(args: Array<String>) {
    println("Byte range: ${Byte.MIN_VALUE} : ${Byte.MAX_VALUE}")
    println("Short range: ${Short.MIN_VALUE} : ${Short.MAX_VALUE}")
    println("Int range: ${Int.MIN_VALUE} : ${Int.MAX_VALUE}")
    
    val baos = ByteArrayOutputStream()
    val msgpack = MessagePack()

    msgpack.packNil(baos)
    println("MsgPack NIL -> ${baos.toByteArray().toHexString()}")
    baos.reset()
    msgpack.packBoolean(true, baos)
    println("MsgPack TRUE -> ${baos.toByteArray().toHexString()}")
    baos.reset()
    msgpack.packBoolean(false, baos)
    println("MsgPack FALSE -> ${baos.toByteArray().toHexString()}")
    /*
    msgpack.reset()
    msgpack.packString("TST")
    println("MsgPack TST -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packByte(0)
    println("MsgPack 0 -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packByte(64)
    println("MsgPack 64 -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packInt(128)
    println("MsgPack 128 -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packInt(255)
    println("MsgPack 255 -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packInt(256)
    println("MsgPack 256 -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packInt(32767)
    println("MsgPack 32767 -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packInt(32768)
    println("MsgPack 32768 -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packInt(65535)
    println("MsgPack 65535 -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packInt(65536)
    println("MsgPack 65536 -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packLong(4294967295L)
    println("MsgPack 4294967295L -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packLong(4294967296L)
    println("MsgPack 4294967296L -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packByte(-1) //  0xff
    println("MsgPack -1 -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packByte(-31) // 0xe1
    println("MsgPack -31 -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packByte(-32) // 0xe0
    println("MsgPack -32 -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packByte(-128) // 0xd080
    println("MsgPack -128 -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packShort(-32767) // 0xd18001
    println("MsgPack -32767 -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packInt(-65536) // 0xd2ffff0000
    println("MsgPack -65536 -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packString("")
    println("MsgPack empty string -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packString("TST") // A3 54 53 54 ; fixstr
    println("MsgPack TST -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packValue(StrValue("compact")) // A7 63 6F 6D 70 61 63 74
    println("MsgPack compact -> ${msgpack.asByteArray().toHexString()}")
    msgpack.reset()
    msgpack.packString("TST;ANDREJS SIBIRCEVS;150482-12101") // 0xd9 00000022 data...
    println("MsgPack 34 BYTE STRING -> ${msgpack.asByteArray().toHexString()}")
    
    data class Tst(val value: String = "TEST")
    val tst1 = Tst()
    msgpack.reset()
    msgpack.packBinArray(asByteArray(tst1))
    println("MsgPack Tst(TEST) -> ${msgpack.asByteArray().toHexString()}")
    */

    baos.reset()
    msgpack.packArray<Int>(arrayOf<Int>(5, 10, 20, 200), baos) // 6 bytes: 94 05 0A 14 CC C8 ; fixarray; C8 eto 200 (CC - tip)
    println("MsgPack intArrayOf(5, 10, 20, 200) -> ${baos.toByteArray().toHexString()}")
    
    baos.reset()
    msgpack.packMap<Int, String>(mapOf<Int, String>(0 to "schema"), baos)
    println("MsgPack mapOf(0 to schema) -> ${baos.toByteArray().toHexString()}")
    
    baos.reset()
    val vMap = ValueMap()
    vMap.put(StrValue("compact"), BoolValue(true))
    vMap.put(StrValue("schema"), IntValue(0))
    msgpack.packValue(vMap, baos) //18 bytes: 82 A7 63 6F 6D 70 61 63 74 C3 A6 73 63 68 65 6D 61 00
    println("MsgPack map(compact: true, schema: 0) -> ${baos.toByteArray().toHexString()}")
    
    baos.reset()
    msgpack.packByte(64, baos)
    val upTst1: java.io.BufferedInputStream = java.io.BufferedInputStream(ByteArrayInputStream(baos.toByteArray()))
    println("64 = ${msgpack.unpackNumber(upTst1.read(), upTst1)}")
    baos.reset()
    msgpack.packInt(255, baos)
    val upTst2: java.io.BufferedInputStream = java.io.BufferedInputStream(ByteArrayInputStream(baos.toByteArray()))
    println("255 = ${msgpack.unpackNumber(upTst2.read(), upTst2)}")
    baos.reset()
    msgpack.packInt(32768, baos)
    val upTst3: java.io.BufferedInputStream = java.io.BufferedInputStream(ByteArrayInputStream(baos.toByteArray()))
    println("32768 = ${msgpack.unpackNumber(upTst3.read(), upTst3)}")
    baos.reset()
    msgpack.packLong(4294967295L, baos)
    val upTst4: java.io.BufferedInputStream = java.io.BufferedInputStream(ByteArrayInputStream(baos.toByteArray()))
    println("4294967295L = ${msgpack.unpackNumber(upTst4.read(), upTst4)}")
    baos.reset()
    msgpack.packByte(-1, baos)
    val upTst5: java.io.BufferedInputStream = java.io.BufferedInputStream(ByteArrayInputStream(baos.toByteArray()))
    println("-1 = ${msgpack.unpackNumber(upTst5.read(), upTst5)}")
    baos.reset()
    msgpack.packByte(-7, baos)
    val upTst6: java.io.BufferedInputStream = java.io.BufferedInputStream(ByteArrayInputStream(baos.toByteArray()))
    println("-7 = ${msgpack.unpackNumber(upTst6.read(), upTst6)}")
    baos.reset()
    msgpack.packInt(-224, baos)
    val upTst7: java.io.BufferedInputStream = java.io.BufferedInputStream(ByteArrayInputStream(baos.toByteArray()))
    println("-224 = ${msgpack.unpackNumber(upTst7.read(), upTst7)}")
    baos.reset()
    msgpack.packInt(-225, baos)
    val upTst8: java.io.BufferedInputStream = java.io.BufferedInputStream(ByteArrayInputStream(baos.toByteArray()))
    println("-225 = ${msgpack.unpackNumber(upTst8.read(), upTst8)}")
    baos.reset()
    msgpack.packInt(-256, baos)
    val upTst9: java.io.BufferedInputStream = java.io.BufferedInputStream(ByteArrayInputStream(baos.toByteArray()))
    println("-256 = ${msgpack.unpackNumber(upTst9.read(), upTst9)}")

    baos.reset()
    msgpack.packArray<Int>(arrayOf<Int>(5, 10, 20, 200), baos) // 6 bytes: 94 05 0A 14 CC C8 ; fixarray; C8 eto 200 (CC - tip)
    val upTst10: java.io.BufferedInputStream = java.io.BufferedInputStream(java.io.ByteArrayInputStream(baos.toByteArray()))
    println("intArrayOf(5, 10, 20, 200) = ")
    val uv1 = msgpack.unpackValue(upTst10)
    when (uv1) {
        is ValueArray -> for(elem in uv1) println(elem)
        else -> println("Is not ValueArray")
    }
    
    val bais11 = ByteArrayInputStream(byteArrayOf())
    val upTst11: java.io.BufferedInputStream = java.io.BufferedInputStream(bais11)
    val uv2 = msgpack.unpackValue(upTst11)
}