package dev.yarlson.yar.highlighting

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import dev.yarlson.yar.YarIcons
import javax.swing.Icon

class YarColorSettingsPage : ColorSettingsPage {

    override fun getIcon(): Icon = YarIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = YarSyntaxHighlighter()

    override fun getDemoText(): String = DEMO_TEXT

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> =
        ADDITIONAL_TAGS

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "Yar"

    companion object {
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Keyword", YarSyntaxHighlighter.KEYWORD),
            AttributesDescriptor("Number", YarSyntaxHighlighter.NUMBER),
            AttributesDescriptor("String", YarSyntaxHighlighter.STRING),
            AttributesDescriptor("Line comment", YarSyntaxHighlighter.COMMENT),
            AttributesDescriptor("Operator", YarSyntaxHighlighter.OPERATOR),
            AttributesDescriptor("Parentheses", YarSyntaxHighlighter.PARENTHESES),
            AttributesDescriptor("Braces", YarSyntaxHighlighter.BRACES),
            AttributesDescriptor("Brackets", YarSyntaxHighlighter.BRACKETS),
            AttributesDescriptor("Comma", YarSyntaxHighlighter.COMMA),
            AttributesDescriptor("Dot", YarSyntaxHighlighter.DOT),
            AttributesDescriptor("Semicolon", YarSyntaxHighlighter.SEMICOLON),
            AttributesDescriptor("Type name", YarAnnotator.TYPE_NAME),
            AttributesDescriptor("Function name", YarAnnotator.FUNCTION_NAME),
            AttributesDescriptor("Function call", YarAnnotator.FUNCTION_CALL),
            AttributesDescriptor("Parameter", YarAnnotator.PARAMETER),
            AttributesDescriptor("Field", YarAnnotator.FIELD),
            AttributesDescriptor("Enum case", YarAnnotator.ENUM_CASE),
            AttributesDescriptor("Error literal", YarAnnotator.ERROR_LITERAL),
            AttributesDescriptor("pub modifier", YarAnnotator.PUB_MODIFIER),
        )

        private val ADDITIONAL_TAGS = mapOf(
            "typeName" to YarAnnotator.TYPE_NAME,
            "funcName" to YarAnnotator.FUNCTION_NAME,
            "funcCall" to YarAnnotator.FUNCTION_CALL,
            "param" to YarAnnotator.PARAMETER,
            "field" to YarAnnotator.FIELD,
            "enumCase" to YarAnnotator.ENUM_CASE,
            "errorLit" to YarAnnotator.ERROR_LITERAL,
            "pub" to YarAnnotator.PUB_MODIFIER,
        )

        private val DEMO_TEXT = """
package main

import "strings"

// A simple struct with fields
<pub>pub</pub> struct <typeName>User</typeName> {
    <field>name</field> <typeName>str</typeName>
    <field>age</field> <typeName>i32</typeName>
}

enum <typeName>Status</typeName> {
    <enumCase>Active</enumCase>
    <enumCase>Inactive</enumCase> { <field>reason</field> <typeName>str</typeName> }
}

interface <typeName>Greeter</typeName> {
    <funcName>greet</funcName>(<param>name</param> <typeName>str</typeName>) !<typeName>str</typeName>
}

fn (<param>u</param> <typeName>User</typeName>) <funcName>greet</funcName>(<param>name</param> <typeName>str</typeName>) !<typeName>str</typeName> {
    if <funcCall>strings</funcCall>.<funcCall>contains</funcCall>(<param>name</param>, " ") {
        return <errorLit>error.InvalidName</errorLit>
    }
    return <param>u</param>.<field>name</field> + ": hello, " + <param>name</param>
}

fn <funcName>classify</funcName>(<param>ch</param> <typeName>i32</typeName>) <typeName>str</typeName> {
    // Character literals produce i32 values
    if <param>ch</param> >= 'a' && <param>ch</param> <= 'z' {
        return "lower"
    }
    return "other"
}

<pub>pub</pub> fn <funcName>main</funcName>() <typeName>i32</typeName> {
    user := <typeName>User</typeName>{<field>name</field>: "Alice", <field>age</field>: 30}
    msg := user.<funcCall>greet</funcCall>("Bob") or |err| {
        <funcCall>print</funcCall>("error\n")
        return 1
    }
    <funcCall>print</funcCall>(msg)
    // Implicit pointer dereference: p.field works for *Struct
    p := &<typeName>User</typeName>{<field>name</field>: "Bob", <field>age</field>: 25}
    <funcCall>print</funcCall>(p.<field>name</field>)
    return 0
}
""".trimIndent()
    }
}
