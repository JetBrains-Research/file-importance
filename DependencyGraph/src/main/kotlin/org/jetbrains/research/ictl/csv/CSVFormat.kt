package org.jetbrains.research.ictl.csv

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.modules.*

/**
 * [RFC-4180](https://datatracker.ietf.org/doc/html/rfc4180)
 */
@ExperimentalSerializationApi
sealed class CSVFormat(
    private val separator: String,
    private val lineSeparator: String,
    override val serializersModule: SerializersModule
) : StringFormat {
    private class Custom(separator: String, lineSeparator: String, serializersModule: SerializersModule) :
        CSVFormat(separator, lineSeparator, serializersModule)

    companion object Default : CSVFormat(
        ",", System.lineSeparator(), EmptySerializersModule()
    ) {
        operator fun invoke(
            separator: String = ",",
            lineSeparator: String = System.lineSeparator(),
            serializersModule: SerializersModule = EmptySerializersModule()
        ): CSVFormat =
            Custom(separator, lineSeparator, serializersModule)
    }

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        // TODO: check first line to match the fields
        val lines = string.split(lineSeparator)
        val data = lines.drop(1).map { it.split(separator) }
        return deserializer.deserialize(
            decoder = CSVDecoder(
                data = data,
                serializersModule = serializersModule
            )
        )
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String =
        encodeToString(serializer, value, true)

    inline fun <reified T> encodeToString(value: T, withHeader: Boolean): String =
        encodeToString(serializersModule.serializer(), value, withHeader)

    fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T, withHeader: Boolean): String = buildString {
        var first = true

        if (withHeader) {
            serializer.descriptor.names.forEach {
                if (!first) {
                    append(separator)
                }
                first = false
                append(it)
            }
            append(lineSeparator)
        }

        serializer.serialize(
            encoder = CSVEncoder(this, separator, lineSeparator, serializersModule),
            value = value
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    internal val SerialDescriptor.names: Sequence<String>
        get() = sequence {
            elementDescriptors.forEachIndexed { index, descriptor ->
                val name = getElementName(index)
                when {
                    descriptor.elementsCount == 0 -> yield(name)
                    descriptor.kind == SerialKind.ENUM -> yield(name)
                    descriptor.kind is StructureKind.MAP -> yield(name)
                    descriptor.kind is StructureKind.LIST -> yield(name)
                    else -> yieldAll(descriptor.names)
                }
            }
        }
}