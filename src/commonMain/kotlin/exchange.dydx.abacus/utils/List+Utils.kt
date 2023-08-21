package exchange.dydx.abacus.utils

import kollections.toIList
import kollections.toIMutableList

typealias IList<T> = kollections.List<T>
typealias IMutableList<T> = kollections.MutableList<T>

internal fun <T> IList<T>.mutable(): IMutableList<T> {
    return this as? IMutableList<T> ?: this.toIMutableList()
}
fun <T : Any> iListOfNotNull(vararg elements: T?): IList<T> = elements.filterNotNull().toIList()
