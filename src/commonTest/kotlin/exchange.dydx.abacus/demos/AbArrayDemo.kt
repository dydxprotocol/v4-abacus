package exchange.dydx.abacus.demos

import exchange.dydx.abacus.utils.IList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AbArrayDemo {

    /*
     * The main problem AbArray solves is array equality checks, in a pattern that serializes and
     * deserializes cleanly.
     */
    @Test
    fun testArraySerialization() {
        val array = arrayOf("A", "B", "C")
        val jsonString = Json.encodeToJsonElement(array).toString()

        assertEquals("""["A","B","C"]""", jsonString)

        val decodeArray = Json.decodeFromString<Array<String>>(string = jsonString)
        // Note that these are NOT equal!
        assertNotEquals(array, decodeArray)
        assertContentEquals(array, decodeArray)

        assertEquals(array.first(), decodeArray.first())
    }

    @Test
    fun testAbArraySerialization() {
        val array = listOf("A", "B", "C")
        val jsonString = Json.encodeToJsonElement(array).toString()
        assertEquals(
            """
            ["A","B","C"]
        """.trimIndent(), jsonString
        )
        val decodeArray = Json.decodeFromString<IList<String>>(jsonString)
        // AbArray object handles deep equals checks for free
        assertEquals(array, decodeArray)

        // however, (historically) at the cost of needing to unwrap the array to access it
        // below we will also demonstrate that this is on longer required
//        assertEquals(array.a.first(), decodeArray.a.first())
    }

    /*
     * But arrays are not found in isolation.
     * What about when encoding and decoding the objects that contain arrays?
     * Still a problem.
     */
    @Serializable
    class DomainObject(val intField: Int, val arrField: Array<String>)

    @Test
    fun testDomainObjectSerialization() {
        val domainObject = DomainObject(42, arrayOf("A", "B", "C"))
        val jsonString = Json.encodeToJsonElement(domainObject).toString()

        assertEquals("""{"intField":42,"arrField":["A","B","C"]}""", jsonString)

        val decodeObj = Json.decodeFromString<DomainObject>(string = jsonString)
        // Note that these are NOT equal!
        assertNotEquals(domainObject, decodeObj)
        assertContentEquals(domainObject.arrField, decodeObj.arrField)
    }

    /*
     * We can use kotlin "data" classes to get equals and hashcode for free
     *
     * However, noticed the lint warning now, on `arrField`, because data class is not able to handle this automatically
     * >> Property with 'Array' type in a 'data' class: it is recommended to override 'equals()' and 'hashCode()'
     *
     * Also note that this is not actually a result of @serializable at all, it simply comes up in this context,
     * and generics do tend to make serialization otherwise tricky, but this issue is actually with data classes.
     */
    data class DataDomainObjectWithWarning(val intField: Int, val arrField: Array<String>)

    /*
     * The ide will auto-generate this boiler plate for us which, is ugly but otherwise works
     *
     * But this is code that subtle bugs can be introduced if fields are added or removed.
     */
    @Serializable
    data class DataDomainObjectWithBoilerplate(val intField: Int, val arrField: Array<String>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as DataDomainObjectWithBoilerplate

            if (intField != other.intField) return false
            if (!arrField.contentEquals(other.arrField)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = intField
            result = 31 * result + arrField.contentHashCode()
            return result
        }
    }

    @Test
    fun testDataDomainObjectSerialization() {
        val domainObject = DataDomainObjectWithBoilerplate(42, arrayOf("A", "B", "C"))
        val jsonString = Json.encodeToJsonElement(domainObject).toString()

        assertEquals("""{"intField":42,"arrField":["A","B","C"]}""", jsonString)

        val decodeObj = Json.decodeFromString<DataDomainObjectWithBoilerplate>(string = jsonString)
        // Equality check is now working. However, we have to add this boiler plate to every object
        // with an array
        assertEquals(domainObject, decodeObj)
    }
}

