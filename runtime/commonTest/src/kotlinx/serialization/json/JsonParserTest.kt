/*
 * Copyright 2017-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.serialization.json

import kotlinx.serialization.*
import kotlinx.serialization.test.*
import kotlin.test.*

class JsonParserTest : JsonTestBase() {

    @Test
    fun testQuotedBrace() {
        val tree = parse("""{"x": "{"}""")
        assertTrue("x" in tree)
        assertEquals("{", tree.getAs<JsonLiteral>("x").content)
    }

    private fun parse(input: String) = default.parseJson(input).jsonObject

    @Test
    fun testEmptyKey() {
        val tree = parse("""{"":"","":""}""")
        assertTrue("" in tree)
        assertEquals("", tree.getAs<JsonLiteral>("").content)
    }

    @Test
    fun testEmptyValue() {
        assertFailsWith<JsonDecodingException> {
            parse("""{"X": "foo", "Y"}""")
        }
    }

    @Test
    fun testIncorrectUnicodeEscape() {
        assertFailsWith<JsonDecodingException> {
            parse("""{"X": "\uDD1H"}""")
        }
    }


    @Test
    fun testParseEscapedSymbols() {
        assertEquals(
            StringData("https://t.co/M1uhwigsMT"),
            default.parse(StringData.serializer(), """{"data":"https:\/\/t.co\/M1uhwigsMT"}""")
        )
        assertEquals(StringData("\"test\""), default.parse(StringData.serializer(), """{"data": "\"test\""}"""))
        assertEquals(StringData("\u00c9"), default.parse(StringData.serializer(), """{"data": "\u00c9"}"""))
        assertEquals(StringData("""\\"""), default.parse(StringData.serializer(), """{"data": "\\\\"}"""))
    }

    @Test
    fun testWorkWithNonAsciiSymbols() {
        assertStringFormAndRestored(
            """{"data":"Русские Буквы 🤔"}""",
            StringData("Русские Буквы \uD83E\uDD14"),
            StringData.serializer(),
            printResult = false
        )
    }

    @Test
    fun testTrailingComma() {
        testTrailingComma("{\"id\":0,}")
        testTrailingComma("{\"id\":0  ,}")
        testTrailingComma("{\"id\":0  , ,}")
    }


    private fun testTrailingComma(content: String) {
        val e = assertFailsWith<JsonDecodingException> {  Json.parseJson(content) }
        val msg = e.message!!
        assertTrue(msg.contains("Expected end of the object"))
    }
}
