package plsar.cargo

import plsar.model.Element
import java.util.*

/**
 * Stores @Element objects within
 * the container object under PLSAR.Cache.
 *
 * Retrieval of elements is done via
 *
 * First inject cache in your class file
 *
 * \@Inject
 * val cache : PLSAR.Cache ? = null
 *
 * then :
 * val foo = cache?.getElement("foo") as Foo
 *
 */
class ElementStorage {
    var elements: MutableMap<String?, Element?>

    init {
        elements = HashMap()
    }
}