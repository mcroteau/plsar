package plsar

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import plsar.BaseTest

class PlsarTest : BaseTest() {

    @Test
    fun testGetCache(){
        assertNotNull(cache)
    }

}