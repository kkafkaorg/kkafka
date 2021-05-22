package org.kkafka

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class LibraryTest {
    @Test
    fun testSomeLibraryMethod() {
        val classUnderTest = Library()
        classUnderTest.someLibraryMethod() shouldBe true
    }
}
