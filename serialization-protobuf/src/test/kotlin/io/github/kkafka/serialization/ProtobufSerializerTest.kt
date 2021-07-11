package io.github.kkafka.serialization

import io.kotest.matchers.shouldBe
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.junit.jupiter.api.Test

internal class ProtobufSerializerTest {

    private fun <T> reencode(value: T, serializer: KSerializer<T>): T {
        val s = ProtobufSerializer(serializer)
        val d = ProtobufDeserializer(serializer)
        return d.deserialize(null, s.serialize(null, value))
    }

    @Test
    fun `(de)serializes standard types`() {
        (-10..10).forEach {
            reencode(it, Int.serializer()) shouldBe it
        }

        listOf(
            "A string",
            "A €³£ \uD800\uDF48 한\t UTF8 string",
        ).forEach {
            reencode(it, String.serializer()) shouldBe it
        }
    }

    @Serializable
    data class A(val i: Int, val nested: A? = null)

    @Test
    fun `(de)serializes data classes`() {
        val aa = A(2, A(3, null))

        reencode(aa, A.serializer()) shouldBe aa
    }
}
