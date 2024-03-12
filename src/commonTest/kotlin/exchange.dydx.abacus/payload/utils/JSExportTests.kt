package exchange.dydx.abacus.payload.utils

import exchange.dydx.abacus.utils.IList
import kollections.JsExport
import kollections.iListOf
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

@JsExport
@Serializable
data class DummyClass1(val strings: IList<String>)

@JsExport
@Serializable
data class DummyClass2(val children: IList<DummyClass1>)

class JSExportTests {
    @Test
    fun testJS() {
        val child1 = DummyClass1(iListOf("1", "2", "3"))
        val child2 = DummyClass1(iListOf("11", "12", "13"))
        val parent = DummyClass2(iListOf(child1, child2))

        assertEquals(parent.children[0], child1)
        assertEquals(parent.children[0].strings[0], "1")
    }
}
