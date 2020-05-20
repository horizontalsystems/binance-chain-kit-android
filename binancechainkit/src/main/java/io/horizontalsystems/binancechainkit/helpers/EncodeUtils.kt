package io.horizontalsystems.binancechainkit.helpers

import com.google.protobuf.CodedOutputStream
import java.io.IOException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.bouncycastle.util.encoders.Hex
import java.nio.charset.Charset


object EncodeUtils {

    private var OBJECT_MAPPER: ObjectMapper = ObjectMapper()


    @Throws(JsonProcessingException::class)
    fun toJsonStringSortKeys(`object`: Any): String {
        return OBJECT_MAPPER.writeValueAsString(`object`)
    }

    @Throws(IOException::class)
    fun <T> toObjectFromJsonString(jsonString: String, tClass: Class<T>): T {
        return OBJECT_MAPPER.readValue(jsonString, tClass)
    }

    fun toJsonEncodeBytes(`object`: Any): ByteArray {
        return toJsonStringSortKeys(`object`).toByteArray( Charset.forName("UTF-8"))
    }

    fun hexStringToByteArray(s: String): ByteArray {
        return Hex.decode(s)
    }


    fun bytesToHex(bytes: ByteArray): String {
        return Hex.toHexString(bytes)
    }

    @Throws(IOException::class)
    fun aminoWrap(raw: ByteArray, typePrefix: ByteArray, isPrefixLength: Boolean): ByteArray {
        var totalLen =  (raw.size + typePrefix.size).toLong()

        if (isPrefixLength)
            totalLen += CodedOutputStream.computeUInt64SizeNoTag(totalLen)

        val msg = ByteArray(totalLen.toInt())
        val cos = CodedOutputStream.newInstance(msg)
        if (isPrefixLength)
            cos.writeUInt64NoTag((raw.size + typePrefix.size).toLong())
        cos.write(typePrefix, 0, typePrefix.size)
        cos.write(raw, 0, raw.size)
        cos.flush()

        return msg
    }

}