package dev.yarlson.yar.references

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.yarlson.yar.psi.*

class YarReferenceTest : BasePlatformTestCase() {

    fun testFunctionCallResolvesToDeclaration() {
        val file = myFixture.configureByText(
            "test.yar",
            """
            package main
            fn greet(name str) void {
                print(name)
            }
            fn main() i32 {
                greet("hello")
                return 0
            }
            """.trimIndent()
        )
        val callIdent = findIdentifier(file, "greet", skip = 1)!!
        val ref = YarReference.create(callIdent)
        assertNotNull("greet call should have a reference", ref)

        val resolved = ref!!.resolve()
        assertNotNull("greet reference should resolve", resolved)
        assertInstanceOf(resolved, YarFunctionDecl::class.java)
        assertEquals("greet", (resolved as YarFunctionDecl).name)
    }

    fun testVariableResolvesToShortDecl() {
        val file = myFixture.configureByText(
            "test.yar",
            """
            package main
            fn main() i32 {
                x := 42
                print_int(x)
                return 0
            }
            """.trimIndent()
        )
        val xRef = findIdentifier(file, "x", skip = 1)!!
        val ref = YarReference.create(xRef)
        assertNotNull("x usage should have a reference", ref)

        val resolved = ref!!.resolve()
        assertNotNull("x reference should resolve", resolved)
        assertInstanceOf(resolved, YarShortDeclStmt::class.java)
    }

    fun testParameterResolvesToParam() {
        val file = myFixture.configureByText(
            "test.yar",
            """
            package main
            fn add(a i32, b i32) i32 {
                return a + b
            }
            """.trimIndent()
        )
        val aRef = findIdentifier(file, "a", skip = 1)!!
        val ref = YarReference.create(aRef)
        assertNotNull("a usage should have a reference", ref)

        val resolved = ref!!.resolve()
        assertNotNull("a reference should resolve", resolved)
        assertInstanceOf(resolved, YarParam::class.java)
        assertEquals("a", (resolved as YarParam).name)
    }

    fun testStructNameResolvesToDeclaration() {
        val file = myFixture.configureByText(
            "test.yar",
            """
            package main
            struct Point {
                x i32
                y i32
            }
            fn main() i32 {
                p := Point{x: 1, y: 2}
                return 0
            }
            """.trimIndent()
        )
        val pointRef = findIdentifier(file, "Point", skip = 1)!!
        val ref = YarReference.create(pointRef)
        assertNotNull("Point usage should have a reference", ref)

        val resolved = ref!!.resolve()
        assertNotNull("Point reference should resolve", resolved)
        assertInstanceOf(resolved, YarStructDecl::class.java)
        assertEquals("Point", (resolved as YarStructDecl).name)
    }

    fun testDeclarationNameHasNoReference() {
        val file = myFixture.configureByText(
            "test.yar",
            """
            package main
            fn greet() void {
            }
            """.trimIndent()
        )
        val declIdent = findIdentifier(file, "greet", skip = 0)!!
        val ref = YarReference.create(declIdent)
        assertNull("Declaration name should not have a reference", ref)
    }

    fun testFieldInitNameHasNoReference() {
        val file = myFixture.configureByText(
            "test.yar",
            """
            package main
            struct Point {
                x i32
            }
            fn main() i32 {
                p := Point{x: 1}
                return 0
            }
            """.trimIndent()
        )
        // Find "x" in "Point{x: 1}" -- skip the struct field declaration "x"
        val fieldInitIdent = findIdentifier(file, "x", skip = 1)!!
        val ref = YarReference.create(fieldInitIdent)
        assertNull("Field init name should not have a reference", ref)
    }

    fun testForLoopVariableResolves() {
        val file = myFixture.configureByText(
            "test.yar",
            """
            package main
            fn main() i32 {
                for i := 0; i < 10; i = i + 1 {
                }
                return 0
            }
            """.trimIndent()
        )
        // Find "i" in the condition "i < 10" -- skip the init decl "i"
        val iRef = findIdentifier(file, "i", skip = 1)!!
        val ref = YarReference.create(iRef)
        assertNotNull("i usage should have a reference", ref)

        val resolved = ref!!.resolve()
        assertNotNull("i reference should resolve", resolved)
        assertInstanceOf(resolved, YarShortDeclStmt::class.java)
    }

    private fun findIdentifier(file: PsiElement, name: String, skip: Int): PsiElement? {
        var count = 0
        val allLeaves = PsiTreeUtil.collectElementsOfType(file, PsiElement::class.java)
        for (leaf in allLeaves) {
            if (leaf.node.elementType == YarTypes.IDENTIFIER && leaf.text == name) {
                if (count == skip) return leaf
                count++
            }
        }
        return null
    }
}
