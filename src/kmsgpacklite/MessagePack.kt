package kmsgpacklite

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger

//>kotlinc kmsgpacklite/Converter.kt kmsgpacklite/Value.kt kmsgpacklite/MessagePack.kt
//>kotlin -classpath kmsgpacklite/MessagePackKt.class kmsgpacklite.MessagePackKt

/**
 * MessagePackException class
 */
class MessagePackException : RuntimeException {
    constructor(message: String, ex: Exception?) : super(message, ex) {}
    constructor(message: String) : super(message) {}
    constructor(ex: Exception) : super(ex) {}
}

private fun InputStream.readOrException(): Int {
    val b = this.read()
    if (b == -1)
        throw MessagePackException("Byte was expected but nothing was read")
    return b
}

private fun InputStream.readOrException(ba: ByteArray): Int {
    val i = this.read(ba)
    if (i != ba.size)
        throw MessagePackException("${ba.size} bytes were expected but only $i byte were read")
    return i
}

/**
 * MessagePack class
 *
 * MessagePack is an implementation of the MessagePack binary serialization format.
 * The official specification can be found here:
 * https://github.com/msgpack/msgpack/blob/master/spec.md
 *
 * @author sibircevs
 */
class MessagePack {
    companion object {
        val POSFIXINT_MASK = 0x80

        val FIXMAP_PREFIX = 0x80
        val FIXARRAY_PREFIX = 0x90
        val FIXSTR_PREFIX = 0xa0

        val NIL = 0xc0
        val NEVER_USED = 0xc1
        val FALSE = 0xc2
        val TRUE = 0xc3
        val BIN8 = 0xc4
        val BIN16 = 0xc5
        val BIN32 = 0xc6
        val EXT8 = 0xc7
        val EXT16 = 0xc8
        val EXT32 = 0xc9
        val FLOAT32 = 0xca
        val FLOAT64 = 0xcb
        val UINT8 = 0xcc
        val UINT16 = 0xcd
        val UINT32 = 0xce
        val UINT64 = 0xcf

        val INT8 = 0xd0
        val INT16 = 0xd1
        val INT32 = 0xd2
        val INT64 = 0xd3

        val FIXEXT1 = 0xd4
        val FIXEXT2 = 0xd5
        val FIXEXT4 = 0xd6
        val FIXEXT8 = 0xd7
        val FIXEXT16 = 0xd8

        val STR8 = 0xd9
        val STR16 = 0xda
        val STR32 = 0xdb

        val ARRAY16 = 0xdc
        val ARRAY32 = 0xdd

        val MAP16 = 0xde
        val MAP32 = 0xdf

        val NEGFIXINT_PREFIX = 0xe0
    }
    
    /**
     * Writes a Nil value.
     *
     * This method writes a nil byte.
     *
     * @param[ostream] The outputstream where the Nil value will be written
     * @throws IOException when underlying output throws IOException
     */
    fun packNil(ostream: OutputStream) {
        ostream.write(NIL)
    }
    
    /**
     * Writes a bool format family value.
     *
     * This method writes a true byte or a false byte.
     *
     * @param[b] the boolean to be written
     * @param[ostream] The outputstream where the bool value will be written
     * @throws IOException when underlying output throws IOException
     */
    fun packBoolean(b: Boolean, ostream: OutputStream) {
        /*
         * Bool format family stores false or true in 1 byte.
         * false:
         * +--------+
         * |  0xc2  |
         * +--------+
         * true:
         * +--------+
         * |  0xc3  |
         * +--------+
         */
        ostream.write(if (b) TRUE else FALSE)
    }
    
    /**
     * Writes an int format family value.
     *
     * This method writes the Byte with rang -128:127.
     *
     * @param[b] the byte to be written
     * @param[ostream] The outputstream where the Nil value will be written
     * @throws IOException when underlying output throws IOException
     */
    fun packByte(b: Byte, ostream: OutputStream) {
        // Byte Range -128 : 127
        if (b < -(1 shl 5)) {
            /*
             * int 8 stores a 8-bit signed integer
             * +--------+--------+
             * |  0xd0  |ZZZZZZZZ|
             * +--------+--------+
             */
            ostream.write(INT8)
            ostream.write(b.toInt())
        } else if (b < 0) {
            /*
             * negative fixnum stores 5-bit negative integer
             * +--------+
             * |111YYYYY|
             * +--------+
             */
            ostream.write(NEGFIXINT_PREFIX or b.toInt())
        } else {
            /*
             * positive fixnum stores 7-bit positive integer
             * +--------+
             * |0XXXXXXX|
             * +--------+
             */
            ostream.write(b.toInt())
        }
    }

    /**
     * Writes an int format family value.
     *
     * This method writes the Short with rang -32768:32767.
     *
     * @param[v] the short to be written
     * @param[ostream] The outputstream where the short value will be written
     * @throws IOException when underlying output throws IOException
     */
    fun packShort(v: Short, ostream: OutputStream) {
        // Short Range -32768 : 32767
        if (v < -(1 shl 5)) {
            if (v < -(1 shl 7)) {
                ostream.write(INT16)
                ostream.write(asByteArray(v))
            } else {
                packByte(v.toByte(), ostream)
            }
        } else if (v < (1 shl 7)) { // < 128
            packByte(v.toByte(), ostream)
        } else {
            if (v < (1 shl 8)) { // < 256
                ostream.write(UINT8)
                ostream.write(asByteArray(v).copyOfRange(1,2))
            } else {
                ostream.write(UINT16)
                ostream.write(asByteArray(v))
            }
        }
    }

    /**
     * Writes an int format family value.
     *
     * This method writes the Int with rang -2^31:2^31-1.
     *
     * @param[v] the int to be written
     * @param[ostream] The outputstream where the int value will be written
     * @throws IOException when underlying output throws IOException
     */
    fun packInt(v: Int, ostream: OutputStream) {
        if (v < -(1 shl 5)) {
            if (v < -(1 shl 15)) {
                ostream.write(INT32)
                ostream.write(asByteArray(v))
            } else {
                packShort(v.toShort(), ostream)
            }
        } else if (v < (1 shl 7)) { // < 128
            packByte(v.toByte(), ostream)
        } else if (v < (1 shl 15)) { // < 32768
            packShort(v.toShort(), ostream)
        } else {
            if (v < (1 shl 16)) { // < 65536
                ostream.write(UINT16)
                ostream.write(asByteArray(v).copyOfRange(2,4))
            } else {
                ostream.write(UINT32)
                ostream.write(asByteArray(v))
            }
        }
    }

    /**
     * Writes an int format family value.
     *
     * This method writes the Long with rang -2^63:2^63-1.
     *
     * @param[v] the long to be written
     * @param[ostream] The outputstream where the long value will be written
     * @throws IOException when underlying output throws IOException
     */
    fun packLong(v: Long, ostream: OutputStream) {
        if (v < -(1L shl 5)) {
            if (v < -(1L shl 15)) {
                if (v < -(1L shl 31)) {
                    ostream.write(INT64)
                    ostream.write(asByteArray(v))
                } else {
                    packInt(v.toInt(), ostream)
                }
            }
        } else if (v < (1 shl 7)) { // < 128
            packByte(v.toByte(), ostream)
        } else if (v < (1 shl 15)) { // < 32768
            packShort(v.toShort(), ostream)
        } else if (v < (1 shl 31)) { // < 2^31
            packInt(v.toInt(), ostream)
        } else {
            if (v < (1L shl 32)) { // < 2^32
                ostream.write(UINT32)
                ostream.write(asByteArray(v).copyOfRange(4,8))
            } else {
                ostream.write(UINT64)
                ostream.write(asByteArray(v))
            }
        }
    }

    /**
     * Writes an int format family value.
     *
     * This method writes the BigInteger.
     *
     * @param[bi] the BigInteger to be written
     * @param[ostream] The outputstream where the BigInteger value will be written
     * @throws IOException when underlying output throws IOException
     */
    /*
    fun packBigInteger(bi: BigInteger, ostream: OutputStream) {
        if (bi.bitLength() <= 63) {
            packLong(bi.longValue())
        } else if (bi.bitLength() == 64 && bi.signum() == 1) {
            baos.write(UINT64)
            baos.write(asByteArray(bi.longValue()))
        } else {
            throw IllegalArgumentException("MessagePack cannot serialize BigInteger larger than 2^64-1")
        }
    }
    */
    
    /**
     * Writes a float format family value.
     *
     * This method writes a float 32-bit value.
     *
     * @param[v] the float to be written
     * @param[ostream] The outputstream where the float value will be written
     * @throws IOException when underlying output throws IOException
     */
    fun packFloat(v: Float, ostream: OutputStream) {
        ostream.write(FLOAT32)
        ostream.write(asByteArray(v))
    }

    /**
     * Writes a float format family value.
     *
     * This method writes a double 64-bit value.
     *
     * @param[v] the double to be written
     * @param[ostream] The outputstream where the double value will be written
     * @throws IOException when underlying output throws IOException
     */
    fun packDouble(v: Double, ostream: OutputStream) {
        ostream.write(FLOAT64)
        ostream.write(asByteArray(v))
    }
    
    /**
     * Writes header of a String value.
     *
     * Size must be number of bytes of a string in UTF-8 encoding.
     *
     * @param[size] the size of the raw data to be written
     * @param[ostream] The outputstream where the size value will be written
     * @throws IOException when underlying output throws IOException
     */
    fun packRawStringHeader(size: Int, ostream: OutputStream) {
        if (size < (1 shl 5)) {
            ostream.write((FIXSTR_PREFIX or size).toInt()) // fixstr stores a byte array whose length is upto 31 bytes
        } else if (size < (1 shl 8)) {
            ostream.write(STR8) // str 8 stores a byte array whose length is upto (2^8)-1 bytes
            ostream.write(asByteArray(size))
        } else if (size < (1 shl 16)) {
            ostream.write(STR16) // str 16 stores a byte array whose length is upto (2^16)-1 bytes
            ostream.write(asByteArray(size))
        } else {
            ostream.write(STR32) // str 32 stores a byte array whose length is upto (2^32)-1 bytes
            ostream.write(asByteArray(size))
        }
    }
    
    /**
     * Writes a String value in UTF-8 encoding.
     *
     * Str format family stores a byte array in 1, 2, 3, or 5 bytes of extra bytes in addition to the size of the byte array.
     * This method writes a UTF-8 string.
     *
     * @param[s] the string to be written
     * @param[ostream] The outputstream where the size value will be written
     * @throws IOException when underlying output throws IOException
     */
    fun packString(s: String, ostream: OutputStream) {
        if (s.length <= 0) {
            packRawStringHeader(0, ostream)
        } else {
            val bytes = s.toByteArray(Charsets.UTF_8)
            packRawStringHeader(bytes.size, ostream)
            ostream.write(bytes)
        }
    }
    
    /**
     * Writes a dynamically typed value.
     *
     * @param[v] the string to be written
     * @param[ostream] The outputstream where the size value will be written
     * @throws IOException when underlying output throws IOException
     */
    fun packValue(v: Any, ostream: OutputStream) {
        when (v) {
            is Boolean -> packBoolean(v, ostream)
            is Byte -> packByte(v, ostream)
            is Short -> packShort(v, ostream)
            is Int -> packInt(v, ostream)
            is Long -> packLong(v, ostream)
            //is BigInteger ->
            is Float -> packFloat(v, ostream)
            is Double -> packDouble(v, ostream)
            is String -> packString(v, ostream)
            
            is NilValue -> packNil(ostream)
            is BoolValue -> packBoolean(v.value, ostream)
            is IntValue -> packLong(v.value, ostream)
            is FloatValue ->
                if (v.value.compareTo(Float.MAX_VALUE) <= 0 && v.value.compareTo(Float.MIN_VALUE) >= 0)
                    packFloat(v.value.toFloat(), ostream)
                else
                    packDouble(v.value, ostream)
            is StrValue -> packString(v.value, ostream)
            is BinValue -> packBinArray(v.value, ostream)
            is ValueArray -> {
                packArrayHeader(v.size, ostream)
                for (elem in v)
                    packValue(elem, ostream)
            }
            is ValueMap -> {
                packMapHeader(v.size, ostream)
                for ((key, value) in v) {
                    packValue(key, ostream)
                    packValue(value, ostream)
                }
            }
            is java.io.Serializable -> packBinArray(asByteArray(v), ostream)
            else -> throw IllegalArgumentException("Value type is not serializable")
        }
    }
    
    /**
     * Writes header of a Binary value.
     * 
     * Size must be number of bytes of a binary array.
     *
     * @param[size] the size of the raw data to be written
     * @param[ostream] The outputstream where the size value will be written
     * @throws IOException when underlying output throws IOException
     */
    fun packBinaryHeader(size: Int, ostream: OutputStream) {
        if (size < (1 shl 8)) {
            ostream.write(BIN8) // bin 8 stores a byte array whose length is upto (2^8)-1 bytes
        } else if (size < (1 shl 16)) {
            ostream.write(BIN16) // bin 16 stores a byte array whose length is upto (2^16)-1 bytes
        } else {
            ostream.write(BIN32) // bin 32 stores a byte array whose length is upto (2^32)-1 bytes
        }
        ostream.write(asByteArray(size))
    }
    
    /**
     * Writes a Binary array.
     *
     * Bin format family stores an byte array in 2, 3, or 5 bytes of extra bytes in addition to the size of the byte array.
     *
     * @param[ba] the byte array to be written
     * @param[ostream] The outputstream where the size value will be written
     * @throws IOException when underlying output throws IOException
     */
    fun packBinArray(ba: ByteArray, ostream: OutputStream) {
        packBinaryHeader(ba.size, ostream)
        ostream.write(ba)
    }

    /**
     * Writes header of an Array value.
     *
     * Size must be number of array elements.
     * 
     * @param[size] the size of array to be written
     * @param[ostream] The outputstream where the size value will be written
     * @throws IOException when underlying output throws IOException
     */
    fun packArrayHeader(size: Int, ostream: OutputStream) {
        if (size < (1 shl 4)) {
            ostream.write((FIXARRAY_PREFIX or size).toInt()) // fixarray stores an array whose length is upto 15 elements
        } else if (size < (1 shl 16)) {
            ostream.write(ARRAY16) // array 16 stores an array whose length is upto (2^16)-1 elements
            ostream.write(asByteArray(size))
        } else {
            ostream.write(ARRAY32) // array 32 stores an array whose length is upto (2^32)-1 elements
            ostream.write(asByteArray(size))
        }
    }
    
    /**
     * Writes an Object array.
     *
     * Array format family stores a sequence of elements in 1, 3, or 5 bytes of extra bytes in addition to the elements.
     *
     * @param[T] the type of the array
     * @param[a] the array to be written
     * @param[ostream] The outputstream where the size value will be written
     * @throws IOException when underlying output throws IOException
     */
    fun <T : Any> packArray(a: Array<T>, ostream: OutputStream) {
        packArrayHeader(a.size, ostream)
        for (elem in a) {
            packValue(elem, ostream)
        }
    }
    
    /**
     * Writes header of a Map value.
     * 
     * Size must be number of map pairs.
     *
     * @param[size] the size of array to be written
     * @param[ostream] The outputstream where the size value will be written
     * @throws IOException when underlying output throws IOException
     */
    fun packMapHeader(size: Int, ostream: OutputStream) {
        if (size < (1 shl 4)) {
            ostream.write((FIXMAP_PREFIX or size).toInt()) // fixmap stores a map whose length is upto 15 elements
        } else if (size < (1 shl 16)) {
            ostream.write(MAP16) // map 16 stores a map whose length is upto (2^16)-1 elements
            ostream.write(asByteArray(size))
        } else {
            ostream.write(MAP32) // map 32 stores a map whose length is upto (2^32)-1 elements
            ostream.write(asByteArray(size))
        }
    }
    
    /**
     * Writes a map.
     *
     * Map format family stores a sequence of key-value pairs in 1, 3, or 5 bytes of extra bytes in addition to the key-value pairs.
     *
     * @param[K] the type of the map key
     * @param[V] the type of the map value
     * @param[m] the map to be written
     * @param[ostream] The outputstream where the size value will be written
     * @throws IOException when underlying output throws IOException
     */
    fun <K : Any, V : Any> packMap(m: Map<K, V>, ostream: OutputStream) {
        packMapHeader(m.size, ostream)
        for ((key, value) in m) {
            packValue(key, ostream)
            packValue(value, ostream)
        }
    }
    
    /**
     * Writes header of an Extension value.
     * 
     * @param[extType] the extension type tag to be written
     * @param[size] number of bytest to be written
     * @throws IOException when underlying output throws IOException
     */
    fun packExtensionTypeHeader(extType: Byte, size: Int, ostream: OutputStream) {
        if (size < (1 shl 8)) {
            if (size == 1) {
                ostream.write(FIXEXT1) // fixext 1 stores an integer and a byte array whose length is 1 byte
                //ostream.write(asByteArray(extType))
            } else if (size == 2) {
                ostream.write(FIXEXT2) // fixext 2 stores an integer and a byte array whose length is 2 bytes
                //ostream.write(asByteArray(extType))
            } else if (size == 4) {
                ostream.write(FIXEXT4) // fixext 4 stores an integer and a byte array whose length is 4 bytes
                //ostream.write(asByteArray(extType))
            } else if (size == 8) {
                ostream.write(FIXEXT8) // fixext 8 stores an integer and a byte array whose length is 8 bytes
                //ostream.write(asByteArray(extType))
            } else if (size == 16) {
                ostream.write(FIXEXT16) // fixext 16 stores an integer and a byte array whose length is 16 bytes
                //ostream.write(asByteArray(extType))
            } else {
                ostream.write(EXT8) // ext 8 stores an integer and a byte array whose length is upto (2^8)-1 bytes
                ostream.write(asByteArray(size))
                //ostream.write(asByteArray(extType))
            }
        } else if (size < (1 shl 16)) {
            ostream.write(EXT16) // ext 16 stores an integer and a byte array whose length is upto (2^16)-1 bytes
            ostream.write(asByteArray(size))
            //ostream.write(asByteArray(extType))
        } else {
            ostream.write(EXT32) // ext 32 stores an integer and a byte array whose length is upto (2^32)-1 bytes
            ostream.write(asByteArray(size))
            //ostream.write(asByteArray(extType))
        }
        ostream.write(extType.toInt())
    }

    /**
     * Try to unpack Value from inputstream
     * 
     * @param[istream] The inputstream from where the byte array will be read and unpacked
     * @return the unpacked Value
     * @throws IOException when underlying input throws IOException
     * @throws MessagePackException when underlying input throws MessagePackException
     */
    fun unpackValue(istream: InputStream): Value {
        val first_byte: Int = istream.read()
        
        val res: Value = when (first_byte) {
            -1 -> Nothing()
            /*
             * nil:
             * +--------+
             * |  0xc0  |
             * +--------+
             */
            NIL -> NilValue()
            /*
             * false:
             * +--------+
             * |  0xc2  |
             * +--------+
             * true:
             * +--------+
             * |  0xc3  |
             * +--------+
             */
            FALSE -> BoolValue(false)
            TRUE -> BoolValue(true)
            /*
             * Details in unpackNumber() function
             */
            in NEGFIXINT_PREFIX..0xff -> IntValue(unpackNumber(first_byte, istream).toLong())
            in 0..0x7f -> IntValue(unpackNumber(first_byte, istream).toLong())
            INT8, INT16, INT32, INT64, UINT8, UINT16, UINT32, UINT64 -> IntValue(unpackNumber(first_byte, istream).toLong())
            FLOAT32 -> FloatValue(unpackNumber(first_byte, istream).toDouble())
            FLOAT64 -> FloatValue(unpackNumber(first_byte, istream).toDouble())
            /*
             * Details in unpackString() function
             */
            in FIXSTR_PREFIX..0xbf -> StrValue(unpackString(first_byte, istream))
            STR8, STR16, STR32 -> StrValue(unpackString(first_byte, istream))
            /*
             * Details in unpackBinArray() function
             */
            BIN8, BIN16, BIN32 -> BinValue(unpackBinArray(first_byte, istream))
            /*
             * Details in unpackArray() function
             */
            in FIXARRAY_PREFIX..0x9f -> unpackArray(first_byte, istream)
            ARRAY16, ARRAY32 ->  unpackArray(first_byte, istream)
            /*
            in FIXMAP_PREFIX..0x8f ->
            MAP16, MAP32 ->
            */
            else -> throw MessagePackException("Unsupported data type: ${first_byte.toByte().toHexString()}")
        }
        return res
    }
    
    /**
     * Try to unpack Number from inputstream
     * 
     * @param[first_byte] The first byte of the stream
     * @param[istream] The inputstream from where the byte array will be read and unpacked
     * @return the unpacked Number
     * @throws IOException when underlying input throws IOException
     * @throws MessagePackException when underlying input throws MessagePackException
     */
    fun unpackNumber(first_byte: Int, istream: InputStream): Number =
        when (first_byte) {
            /*
             * negative fixnum stores 5-bit negative integer
             * +--------+
             * |111YYYYY|
             * +--------+
             * 111YYYYY is 8-bit signed integer
             *
             * int 8 stores a 8-bit signed integer
             * +--------+--------+
             * |  0xd0  |ZZZZZZZZ|
             * +--------+--------+
             * int 16 stores a 16-bit big-endian signed integer
             * +--------+--------+--------+
             * |  0xd1  |ZZZZZZZZ|ZZZZZZZZ|
             * +--------+--------+--------+
             * int 32 stores a 32-bit big-endian signed integer
             * +--------+--------+--------+--------+--------+
             * |  0xd2  |ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|
             * +--------+--------+--------+--------+--------+
             * int 64 stores a 64-bit big-endian signed integer
             * +--------+--------+--------+--------+--------+--------+--------+--------+--------+
             * |  0xd3  |ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|
             * +--------+--------+--------+--------+--------+--------+--------+--------+--------+
             */
            in NEGFIXINT_PREFIX..0xff ->
                (first_byte - 0xff - 1).toByte()
            INT8 ->
                istream.readOrException().toByte()
            INT16 -> { // < 32768
                val ba = ByteArray(2)
                istream.readOrException(ba)
                asShortFromArray(ba)
            }
            INT32 -> { // < 2^31
                val ba = ByteArray(4)
                istream.readOrException(ba)
                asIntFromArray(ba)
            }
            INT64 -> {
                val ba = ByteArray(8)
                istream.readOrException(ba)
                asLongFromArray(ba)
            }
            /*
             * positive fixnum stores 7-bit positive integer
             * +--------+
             * |0XXXXXXX|
             * +--------+
             * 0XXXXXXX is 8-bit unsigned integer
             *
             * uint 8 stores a 8-bit unsigned integer
             * +--------+--------+
             * |  0xcc  |ZZZZZZZZ|
             * +--------+--------+
             * uint 16 stores a 16-bit big-endian unsigned integer
             * +--------+--------+--------+
             * |  0xcd  |ZZZZZZZZ|ZZZZZZZZ|
             * +--------+--------+--------+
             * uint 32 stores a 32-bit big-endian unsigned integer
             * +--------+--------+--------+--------+--------+
             * |  0xce  |ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|
             * +--------+--------+--------+--------+--------+
             * uint 64 stores a 64-bit big-endian unsigned integer
             * +--------+--------+--------+--------+--------+--------+--------+--------+--------+
             * |  0xcf  |ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|
             * +--------+--------+--------+--------+--------+--------+--------+--------+--------+
             */
            in 0..0x7f -> // < 128
                first_byte.toByte()
            UINT8 -> // < 255
                istream.readOrException().toShort()
            UINT16 -> { // < 65535
                val ba = ByteArray(2)
                istream.readOrException(ba)
                val uShortV = asShortFromArray(ba)
                if (uShortV < 0) uShortV.toInt() and 0xffff else uShortV
            }
            UINT32 -> { // < 2^32
                val ba = ByteArray(4)
                istream.readOrException(ba)
                val uIntV = asIntFromArray(ba)
                if (uIntV < 0) uIntV.toLong() and 0xffffffff else uIntV
            }
            UINT64 -> {
                val ba = ByteArray(8)
                istream.readOrException(ba)
                asLongFromArray(ba)
            }
            /*
             * float 32 stores a floating point number in IEEE 754 single precision floating point number format:
             * +--------+--------+--------+--------+--------+
             * |  0xca  |XXXXXXXX|XXXXXXXX|XXXXXXXX|XXXXXXXX|
             * +--------+--------+--------+--------+--------+
             * float 64 stores a floating point number in IEEE 754 double precision floating point number format:
             * +--------+--------+--------+--------+--------+--------+--------+--------+--------+
             * |  0xcb  |YYYYYYYY|YYYYYYYY|YYYYYYYY|YYYYYYYY|YYYYYYYY|YYYYYYYY|YYYYYYYY|YYYYYYYY|
             * +--------+--------+--------+--------+--------+--------+--------+--------+--------+
             */
            FLOAT32 -> {
                val ba = ByteArray(4)
                istream.readOrException(ba)
                asFloatFromArray(ba)
            }
            FLOAT64 -> {
                val ba = ByteArray(8)
                istream.readOrException(ba)
                asDoubleFromArray(ba)
            }
            else -> throw MessagePackException("Unsupported number type: ${first_byte.toByte().toHexString()}")
        }
    
    //fun unpackNumber(istream: InputStream): Number = unpackNumber(istream.read(), istream)
    
    /**
     * Try to unpack String from inputstream
     * 
     * @param[first_byte] The first byte of the stream
     * @param[istream] The inputstream from where the byte array will be read and unpacked
     * @return the unpacked String
     * @throws IOException when underlying input throws IOException
     * @throws MessagePackException when underlying input throws MessagePackException
     */
    fun unpackString(first_byte: Int, istream: InputStream): String {
        val len: Int =
            when (first_byte) {
                /*
                 * fixstr stores a byte array whose length is upto 31 bytes:
                 * +--------+========+
                 * |101XXXXX|  data  |
                 * +--------+========+
                 * str 8 stores a byte array whose length is upto (2^8)-1 bytes:
                 * +--------+--------+========+
                 * |  0xd9  |YYYYYYYY|  data  |
                 * +--------+--------+========+
                 * str 16 stores a byte array whose length is upto (2^16)-1 bytes:
                 * +--------+--------+--------+========+
                 * |  0xda  |ZZZZZZZZ|ZZZZZZZZ|  data  |
                 * +--------+--------+--------+========+
                 * str 32 stores a byte array whose length is upto (2^32)-1 bytes:
                 * +--------+--------+--------+--------+--------+========+
                 * |  0xdb  |AAAAAAAA|AAAAAAAA|AAAAAAAA|AAAAAAAA|  data  |
                 * +--------+--------+--------+--------+--------+========+
                 */
                in FIXSTR_PREFIX..0xbf -> first_byte - FIXSTR_PREFIX
                STR8 -> istream.readOrException()
                STR16 -> {
                    val ba = ByteArray(2)
                    istream.readOrException(ba)
                    asShortFromArray(ba).toInt()
                }
                STR32 -> {
                    val ba = ByteArray(4)
                    istream.readOrException(ba)
                    asIntFromArray(ba)
                }
                else -> throw MessagePackException("Data type is not string")
            }
        val bytes = ByteArray(len)
        istream.readOrException(bytes)
        return String(bytes, Charsets.UTF_8)
    }
    
    //fun unpackString(istream: InputStream): String = unpackString(istream.read(), istream)

    /**
     * Try to unpack Binary Array from inputstream
     * 
     * @param[first_byte] The first byte of the stream
     * @param[istream] The inputstream from where the byte array will be read and unpacked
     * @return the unpacked ByteArray
     * @throws IOException when underlying input throws IOException
     * @throws MessagePackException when underlying input throws MessagePackException
     */
    fun unpackBinArray(first_byte: Int, istream: InputStream): ByteArray {
        val len: Int =
            when (first_byte) {
                /*
                 * bin 8 stores a byte array whose length is upto (2^8)-1 bytes:
                 * +--------+--------+========+
                 * |  0xc4  |XXXXXXXX|  data  |
                 * +--------+--------+========+
                 * bin 16 stores a byte array whose length is upto (2^16)-1 bytes:
                 * +--------+--------+--------+========+
                 * |  0xc5  |YYYYYYYY|YYYYYYYY|  data  |
                 * +--------+--------+--------+========+
                 * bin 32 stores a byte array whose length is upto (2^32)-1 bytes:
                 * +--------+--------+--------+--------+--------+========+
                 * |  0xc6  |ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|  data  |
                 * +--------+--------+--------+--------+--------+========+
                 */
                BIN8 -> istream.readOrException()
                BIN16 -> {
                    val ba = ByteArray(2)
                    istream.readOrException(ba)
                    asShortFromArray(ba).toInt()
                }
                BIN32 -> {
                    val ba = ByteArray(4)
                    istream.readOrException(ba)
                    asIntFromArray(ba)
                }
                else -> throw MessagePackException("Data type is not byte array")
            }
        val bytes = ByteArray(len)
        istream.readOrException(bytes)
        return bytes
    }
    
    //fun unpackBinArray(istream: InputStream): ByteArray = unpackBinArray(istream.read(), istream)
    
    /**
     * Try to unpack Array from inputstream
     * 
     * @param[first_byte] The first byte of the stream
     * @param[istream] The inputstream from where the byte array will be read and unpacked
     * @return the unpacked ValueArray
     * @throws IOException when underlying input throws IOException
     * @throws MessagePackException when underlying input throws MessagePackException
     */
    fun unpackArray(first_byte: Int, istream: InputStream): ValueArray {
        val res = ValueArray()
        val len: Int =
            when (first_byte) {
                /*
                 * fixarray stores an array whose length is upto 15 elements:
                 * +--------+~~~~~~~~~~~~~~~~~+
                 * |1001XXXX|    N objects    |
                 * +--------+~~~~~~~~~~~~~~~~~+
                 * array 16 stores an array whose length is upto (2^16)-1 elements:
                 * +--------+--------+--------+~~~~~~~~~~~~~~~~~+
                 * |  0xdc  |YYYYYYYY|YYYYYYYY|    N objects    |
                 * +--------+--------+--------+~~~~~~~~~~~~~~~~~+
                 * array 32 stores an array whose length is upto (2^32)-1 elements:
                 * +--------+--------+--------+--------+--------+~~~~~~~~~~~~~~~~~+
                 * |  0xdd  |ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|    N objects    |
                 * +--------+--------+--------+--------+--------+~~~~~~~~~~~~~~~~~+
                 */
                in FIXARRAY_PREFIX..0x9f -> first_byte - FIXARRAY_PREFIX
                ARRAY16 -> {
                    val ba = ByteArray(2)
                    istream.readOrException(ba)
                    asShortFromArray(ba).toInt()
                }
                ARRAY32 ->  {
                    val ba = ByteArray(4)
                    istream.readOrException(ba)
                    asIntFromArray(ba)
                }
                else -> throw MessagePackException("Data type is not array")
            }

        (1..len).forEach { res.add(unpackValue(istream)) }
        return res
    }
    
    /**
     * Try to unpack Map from inputstream
     * 
     * @param[first_byte] The first byte of the stream
     * @param[istream] The inputstream from where the byte array will be read and unpacked
     * @return the unpacked ValueMap
     * @throws IOException when underlying input throws IOException
     * @throws MessagePackException when underlying input throws MessagePackException
     */
    fun unpackMap(first_byte: Int, istream: InputStream): ValueMap {
        val res = ValueMap()
        val len: Int =
            when (first_byte) {
                /*
                 * fixmap stores a map whose length is upto 15 elements
                 * +--------+~~~~~~~~~~~~~~~~~+
                 * |1000XXXX|   N*2 objects   |
                 * +--------+~~~~~~~~~~~~~~~~~+
                 * map 16 stores a map whose length is upto (2^16)-1 elements
                 * +--------+--------+--------+~~~~~~~~~~~~~~~~~+
                 * |  0xde  |YYYYYYYY|YYYYYYYY|   N*2 objects   |
                 * +--------+--------+--------+~~~~~~~~~~~~~~~~~+
                 * map 32 stores a map whose length is upto (2^32)-1 elements
                 * +--------+--------+--------+--------+--------+~~~~~~~~~~~~~~~~~+
                 * |  0xdf  |ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|   N*2 objects   |
                 * +--------+--------+--------+--------+--------+~~~~~~~~~~~~~~~~~+
                 */
                in FIXMAP_PREFIX..0x8f -> first_byte - FIXMAP_PREFIX
                MAP16 -> {
                    val ba = ByteArray(2)
                    istream.readOrException(ba)
                    asShortFromArray(ba).toInt()
                }
                MAP32 ->  {
                    val ba = ByteArray(4)
                    istream.readOrException(ba)
                    asIntFromArray(ba)
                }
                else -> throw MessagePackException("Data type is not array")
            }

        (1..len).forEach { res.put(unpackValue(istream), unpackValue(istream)) }
        return res
    }
}
