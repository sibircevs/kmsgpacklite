package kmsgpacklite

/*
 * Deserialization: format to type conversion
 *
 * MessagePack deserializers convert MessagePack formats into types as following:
 * source formats                                                           output type
 * positive fixint, negative fixint, int 8/16/32/64 and uint 8/16/32/64     Integer
 * nil                                                                      Nil
 * false and true                                                           Boolean
 * float 32/64                                                              Float
 * fixstr and str 8/16/32                                                   String
 * bin 8/16/32                                                              Binary
 * fixarray and array 16/32                                                 Array
 * fixmap map 16/32                                                         Map
 * fixext and ext 8/16/32                                                   Extension
 */

/**
 * The root of the Value classes hierarchy.
 */
abstract class Value

/**
 * The root of the number classes hierarchy.
 */
abstract class NumberValue : Value() {
    /**
     * Returns the value of this number as a Byte, which may involve rounding or truncation.
     */
    abstract fun toByte(): Byte

    /**
     * Returns the Char with the numeric value equal to this number, truncated to 16 bits if appropriate.
     */
    abstract fun toChar(): Char

    /**
     * Returns the value of this number as a Double, which may involve rounding.
     */
    abstract fun toDouble(): Double

    /**
     * Returns the value of this number as a Float, which may involve rounding.
     */
    abstract fun toFloat(): Float

    /**
     * Returns the value of this number as an Int, which may involve rounding or truncation.
     */
    abstract fun toInt(): Int

    /**
     * Returns the value of this number as a Long, which may involve rounding or truncation.
     */
    abstract fun toLong(): Long

    /**
     * Returns the value of this number as a Short, which may involve rounding or truncation.
     */
    abstract fun toShort(): Short
}

/**
 * The root of the string and binary classes.
 */
abstract class RawValue : Value()

/**
 * Result if the stream is empty
 */
class Nothing: Value()

/**
 * Representation of MessagePack's Nil format family.
 */
class NilValue: Value()

/**
 * Representation of MessagePack's Bool format family.
 *
 * @property value the Boolean value
 */
class BoolValue(val value: Boolean) : Value() {
    override fun toString() = value.toString()
}

/**
 * Representation of MessagePack's Int format family
 *
 * @property value the Long value of integer format family
 */
class IntValue(val value: Long) : NumberValue() {
    /**
     * Returns the value of this number as a Byte, which may involve rounding or truncation.
     */
    override fun toByte(): Byte = value.toByte()

    /**
     * Returns the Char with the numeric value equal to this number, truncated to 16 bits if appropriate.
     */
    override fun toChar(): Char = value.toChar()

    /**
     * Returns the value of this number as a Double, which may involve rounding.
     */
    override fun toDouble(): Double = value.toDouble()

    /**
     * Returns the value of this number as a Float, which may involve rounding.
     */
    override fun toFloat(): Float = value.toFloat()

    /**
     * Returns the value of this number as an Int, which may involve rounding or truncation.
     */
    override fun toInt(): Int = value.toInt()

    /**
     * Returns the value of this number as a Long, which may involve rounding or truncation.
     */
    override fun toLong(): Long = value

    /**
     * Returns the value of this number as a Short, which may involve rounding or truncation.
     */
    override fun toShort(): Short = value.toShort()
    
    override fun toString() = value.toString()
}

/**
 * Representation of MessagePack's Float format family.
 *
 * @property value the Double value of float format family
 */
class FloatValue(val value: Double) : NumberValue() {
    /**
     * Returns the value of this number as a Byte, which may involve rounding or truncation.
     */
    override fun toByte(): Byte = value.toByte()

    /**
     * Returns the Char with the numeric value equal to this number, truncated to 16 bits if appropriate.
     */
    override fun toChar(): Char = value.toChar()

    /**
     * Returns the value of this number as a Double, which may involve rounding.
     */
    override fun toDouble(): Double = value

    /**
     * Returns the value of this number as a Float, which may involve rounding.
     */
    override fun toFloat(): Float = value.toFloat()

    /**
     * Returns the value of this number as an Int, which may involve rounding or truncation.
     */
    override fun toInt(): Int = value.toInt()

    /**
     * Returns the value of this number as a Long, which may involve rounding or truncation.
     */
    override fun toLong(): Long = value.toLong()

    /**
     * Returns the value of this number as a Short, which may involve rounding or truncation.
     */
    override fun toShort(): Short = value.toShort()
    
    override fun toString() = value.toString()
}

/**
 * Representation of MessagePack's Str format family.
 *
 * @property value the String value
 */
class StrValue(val value: String) : RawValue() {
    override fun toString() = value
}

/**
 * Representation of MessagePack's Bin format family.
 *
 * @property value the ByteArray value
 */
class BinValue(val value: ByteArray) : RawValue() {
    override fun toString() = String(value, Charsets.UTF_8)
    fun toHexString() = value.toHexString()
}

/**
 * Representation of MessagePack's Array format family.
 *
 * @constructor Creates the array with defined elements.
 */
class ValueArray(elements: Collection<Value>) : Value(), MutableList<Value> {
    private val _items = ArrayList<Value>(elements)
    
    constructor() : this(emptyList<Value>())
    
    /**
     * Returns the size of the collection.
	 */
    override val size: Int
        get() = _items.size

    /**
     * Adds the specified element to the collection.
     */
    override fun add(element: Value): Boolean = _items.add(element)

    /**
     * Inserts an element into the list at the specified index.
     */
    override fun add(index: Int, element: Value) { _items.add(index, element) }

    /**
     * Adds all of the elements in the specified collection to this collection.
     */
    override fun addAll(elements: Collection<Value>): Boolean = _items.addAll(elements)

    /**
     * Inserts all of the elements in the specified collection elements into this list at the specified index.
     */
    override fun addAll(index: Int, elements: Collection<Value>): Boolean = _items.addAll(index, elements)

    /**
     * Removes all elements from this collection.
     */
    override fun clear() { _items.clear() }

    /**
     * Returns true if element is found in the collection.
     */
    override fun contains(element: Value): Boolean = _items.contains(element)
    
    /**
     * Checks if all elements in the specified collection are contained in this collection.
     */
    override fun containsAll(elements: Collection<Value>): Boolean = _items.containsAll(elements)
    
    /**
     * Returns first index of element, or -1 if the collection does not contain element.
     */
    override fun indexOf(element: Value): Int = _items.indexOf(element)
    
    /**
     * Returns true if the collection is empty (contains no elements), false otherwise.
     */
    override fun isEmpty(): Boolean = _items.isEmpty()

    /**
	 * Returns an iterator over the elements of this object.
     */
    override fun iterator(): MutableIterator<Value> = _items.iterator()
    
    /**
     * Returns last index of element, or -1 if the collection does not contain element.
     */
    override fun lastIndexOf(element: Value): Int = _items.lastIndexOf(element)

    /**
     * Returns a list iterator over the elements in this list (in proper sequence).
     */
    override fun listIterator(): MutableListIterator<Value> = _items.listIterator()

    /**
     * Returns a list iterator over the elements in this list (in proper sequence), starting at the specified index.
     */
    override fun listIterator(index: Int): MutableListIterator<Value> = _items.listIterator(index)

	/**
     * Removes a single instance of the specified element from this collection, if it is present.
     */
    override fun remove(element: Value): Boolean = _items.remove(element)

	/**
     * Removes all of this collection's elements that are also contained in the specified collection.
     */
    override fun removeAll(elements: Collection<Value>): Boolean = _items.removeAll(elements)

    /**
     * Removes an element at the specified index from the list.
     */
    override fun removeAt(index: Int): Value = _items.removeAt(index)

    /**
     * Retains only the elements in this collection that are contained in the specified collection.
     */
    override fun retainAll(elements: Collection<Value>): Boolean = _items.retainAll(elements)

    /**
     * Replaces the element at the specified position in this list with the specified element.
     */
    override operator fun set(index: Int, element: Value): Value = _items.set(index, element)

    /**
     * Returns a view of the portion of this list between the specified fromIndex (inclusive) and toIndex (exclusive). The returned list is backed by this list, so non-structural changes in the returned list are reflected in this list, and vice-versa.
     */
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Value> = _items.subList(fromIndex, toIndex)

    /**
     * Returns the element at the specified index in the list.
     */
    override operator fun get(index: Int): Value = _items.get(index)

    /**
     * Returns an array containing all of the elements in this list in proper sequence (from first to last element).
     */
    fun toArray(): Array<Value> = _items.toTypedArray()

    /**
     * Returns the value as {@code List}.
     */
    fun toList(): List<Value> = _items
}

/**
 * Representation of MessagePack's Map format family.
 *
 * @constructor Creates the map with defined elements.
 */
class ValueMap(original: Map<Value, Value>): Value(), MutableMap<Value, Value> {
    private val _items = LinkedHashMap<Value, Value>(original)
    
    constructor() : this(emptyMap<Value, Value>())
    
    /**
     * Returns a mutable Collection of all values in this map. Note that this collection may contain duplicate values.
     */
    override val entries: MutableSet<MutableMap.MutableEntry<Value, Value>> = _items.entries

    /**
     * Returns a mutable Set of all keys in this map.
     */
    override val keys: MutableSet<Value> = _items.keys

    /**
     * Returns a mutable Set of all key/value pairs in this map.
     */
    override val values: MutableCollection<Value> = _items.values

    /**
     * Returns the number of key/value pairs in the map.
     */
    override val size: Int
        get() = _items.size

    /**
     * Removes all elements from this map.
     */
    override fun clear() { _items.clear() }

    /**
     * Associates the specified value with the specified key in the map.
     */
    override fun put(key: Value, value: Value): Value? = _items.put(key, value)
    
    /**
     * Updates this map with key/value pairs from the specified map from.
     */
    override fun putAll(from: Map<out Value, Value>) { _items.putAll(from) }
    
    /**
     * Removes the specified key and its corresponding value from this map.
     */
    override fun remove(key: Value): Value? = _items.remove(key)

    /**
     * Returns true if the map is empty (contains no elements), false otherwise.
     */
    override fun isEmpty(): Boolean = _items.isEmpty()

    /**
     * Returns the value corresponding to the given key, or null if such a key is not present in the map.
     */
    override fun get(key: Value): Value? = _items.get(key)

    /**
     * Returns true if the map contains the specified key.
     */
    override fun containsKey(key: Value): Boolean = _items.containsKey(key)

    /**
     * Returns true if the map maps one or more keys to the specified value.
     */
    override fun containsValue(value: Value): Boolean = _items.containsValue(value)
    
}

class ExtensionValue: Value()
