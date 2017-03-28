package kmsgpacklite

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.io.ObjectOutputStream
import java.io.ObjectInputStream

/**
 * Converting 16-bit (2 bytes) short (big endian)
 */
fun asByteArray(value: Short): ByteArray {
    val buffer: ByteBuffer = ByteBuffer.allocate(2)
    buffer.order(ByteOrder.BIG_ENDIAN)
    buffer.putShort(value)
    buffer.flip()
    return buffer.array()
}

fun asShortFromArray(ba: ByteArray): Short {
    val buffer: ByteBuffer = ByteBuffer.wrap(ba)
    buffer.order(ByteOrder.BIG_ENDIAN)
    return buffer.getShort()
}

/**
 * Converting 32-bit (4 bytes) integer (big endian)
 */
fun asByteArray(value: Int): ByteArray {
    val buffer: ByteBuffer = ByteBuffer.allocate(4)
    buffer.order(ByteOrder.BIG_ENDIAN)
    buffer.putInt(value)
    buffer.flip()
    return buffer.array()
}

fun asIntFromArray(ba: ByteArray): Int {
    val buffer: ByteBuffer = ByteBuffer.wrap(ba)
    buffer.order(ByteOrder.BIG_ENDIAN)
    return buffer.getInt()
}

/**
 * Converting 64-bit (8 bytes) integer (big endian)
 */
fun asByteArray(value: Long): ByteArray {
    val buffer: ByteBuffer = ByteBuffer.allocate(8)
    buffer.order(ByteOrder.BIG_ENDIAN)
    buffer.putLong(value)
    buffer.flip()
    return buffer.array()
}

fun asLongFromArray(ba: ByteArray): Long {
    val buffer: ByteBuffer = ByteBuffer.wrap(ba)
    buffer.order(ByteOrder.BIG_ENDIAN)
    return buffer.getLong()
}

/**
 * Converting 32-bit (4 bytes) float (big endian)
 */
fun asByteArray(value: Float): ByteArray {
    val buffer: ByteBuffer = ByteBuffer.allocate(4)
    buffer.order(ByteOrder.BIG_ENDIAN)
    buffer.putFloat(value)
    buffer.flip()
    return buffer.array()
}

fun asFloatFromArray(ba: ByteArray): Float {
    val buffer: ByteBuffer = ByteBuffer.wrap(ba)
    buffer.order(ByteOrder.BIG_ENDIAN)
    return buffer.getFloat()
}

/**
 * Converting 64-bit (8 bytes) double (big endian)
 */
fun asByteArray(value: Double): ByteArray {
    val buffer: ByteBuffer = ByteBuffer.allocate(8)
    buffer.order(ByteOrder.BIG_ENDIAN)
    buffer.putDouble(value)
    buffer.flip()
    return buffer.array()
}

fun asDoubleFromArray(ba: ByteArray): Double {
    val buffer: ByteBuffer = ByteBuffer.wrap(ba)
    buffer.order(ByteOrder.BIG_ENDIAN)
    return buffer.getDouble()
}

/**
 * Converting Any
 * V kotlin poka net svoej normalnoj serilizacii
 */
fun asByteArray(obj: Any): ByteArray {
    val baos = ByteArrayOutputStream()
    val oos = ObjectOutputStream(baos)
    oos.writeObject(obj)
    oos.close()
    return baos.toByteArray()
}

/**
 * Get Any from ByteArray
 */
fun asObject(ba: ByteArray): Any {
    val bais = ByteArrayInputStream(ba)
    val ois = ObjectInputStream(bais)
    return ois.readObject()
}

/**
 *  Set of chars for a half-byte.
 */
private val CHARS = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

/**
 *  Returns the string of two characters representing the HEX value of the byte.
 */
internal fun Byte.toHexString(): String {
    val i = this.toInt()
    val char2 = CHARS[i and 0x0f]
    val char1 = CHARS[i shr 4 and 0x0f]
    return "$char1$char2"
}


/**
 *  Returns the HEX representation of ByteArray data.
 */
internal fun ByteArray.toHexString(): String {
    val builder = StringBuilder()
    for (b in this) {
        builder.append(b.toHexString())
    }
    return builder.toString()
}
