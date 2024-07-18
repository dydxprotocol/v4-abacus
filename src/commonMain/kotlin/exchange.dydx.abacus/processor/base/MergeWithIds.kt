package exchange.dydx.abacus.processor.base

/**
 * Merge two lists of payloads, dropping older items if a new item with the same ID exists.
 */
fun <T> mergeWithIds(
    new: List<T>,
    existing: List<T>,
    id: (T) -> String?,
): List<T> {
    val ids = mutableSetOf<String>()
    val merged = mutableListOf<T>()
    new.forEach { item ->
        id(item)?.let { itemId ->
            ids.add(itemId)
            merged.add(item)
        }
    }
    existing.forEach { item ->
        id(item)?.let { itemId ->
            if (!ids.contains(itemId)) {
                ids.add(itemId)
                merged.add(item)
            }
        }
    }

    return merged
}
