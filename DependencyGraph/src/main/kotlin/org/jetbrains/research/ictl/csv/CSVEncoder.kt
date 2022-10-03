package org.jetbrains.research.ictl.csv

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

@ExperimentalSerializationApi
internal class CSVEncoder(
    private val builder: StringBuilder,
    private val separator: String,
    private val lineSeparator: String,
    override val serializersModule: SerializersModule
) : AbstractEncoder() {
    private var first = true
    private var depth = 0

    override fun encodeValue(value: Any) {
        if (!first) {
            builder.append(separator)
        }
        first = false
        builder.append(value)

    }

    override fun encodeNull() {
        if (!first) {
            builder.append(separator)
        }
        first = false
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
//        if (depth == 0) {
//            builder.append(lineSeparator)
//            first = true
//        }
        depth++
        return this
    }

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder = this

    override fun endStructure(descriptor: SerialDescriptor) {
        depth--
        if (depth == 0) {
            builder.append(lineSeparator)
            first = true
        }
    }

    override fun encodeInline(inlineDescriptor: SerialDescriptor): Encoder {
        if (depth == 0) {
            builder.append(lineSeparator)
            first = true
        }
        return this
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        if (!first) {
            builder.append(separator)
        }
        first = false
        builder.append(enumDescriptor.getElementName(index))
    }
}