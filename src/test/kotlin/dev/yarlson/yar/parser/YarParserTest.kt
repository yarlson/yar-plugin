package dev.yarlson.yar.parser

import com.intellij.testFramework.ParsingTestCase

class YarParserTest : ParsingTestCase("parser", "yar", YarParserDefinition()) {

    override fun getTestDataPath(): String = "src/test/resources/testData"

    override fun skipSpaces(): Boolean = true

    override fun includeRanges(): Boolean = true

    fun testDivide() = doTest(true)
    fun testStructsAndLoops() = doTest(true)
    fun testEnums() = doTest(true)
    fun testMethods() = doTest(true)
    fun testClosures() = doTest(true)
    fun testGenerics() = doTest(true)
    fun testMaps() = doTest(true)
    fun testInterfaces() = doTest(true)
    fun testPointers() = doTest(true)
    fun testConcurrency() = doTest(true)
}
