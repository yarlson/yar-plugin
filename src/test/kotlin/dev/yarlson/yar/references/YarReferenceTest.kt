package dev.yarlson.yar.references

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.yarlson.yar.psi.*

class YarReferenceTest : BasePlatformTestCase() {

    fun testFunctionCallResolvesToDeclaration() {
        myFixture.configureByText(
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
        // Declaration name is inside YarFunctionDecl, not an identExpr.
        // The identExpr "greet" is in the call site -- it's the first identExpr matching "greet".
        val identExpr = findIdentExpr("greet", skip = 0)
        assertNotNull("Should find greet call identExpr", identExpr)

        val ref = identExpr!!.reference
        assertNotNull("greet identExpr should have a reference via mixin", ref)

        val resolved = ref!!.resolve()
        assertNotNull("greet reference should resolve", resolved)
        assertInstanceOf(resolved, YarFunctionDecl::class.java)
        assertEquals("greet", (resolved as YarFunctionDecl).name)
    }

    fun testVariableResolvesToShortDecl() {
        myFixture.configureByText(
            "test.yar",
            """
            package main
            fn main() i32 {
                x := 42
                print(to_str(x))
                return 0
            }
            """.trimIndent()
        )
        // "x" in the short decl is the name of YarShortDeclStmt, not an identExpr.
        // The identExpr "x" is in print(to_str(x)) -- first identExpr matching "x".
        val identExpr = findIdentExpr("x", skip = 0)!!
        val ref = identExpr.reference
        assertNotNull("x identExpr should have a reference", ref)

        val resolved = ref!!.resolve()
        assertNotNull("x reference should resolve", resolved)
        assertInstanceOf(resolved, YarShortDeclStmt::class.java)
    }

    fun testParameterResolvesToParam() {
        myFixture.configureByText(
            "test.yar",
            """
            package main
            fn add(a i32, b i32) i32 {
                return a + b
            }
            """.trimIndent()
        )
        // "a" in param is YarParam name, not an identExpr.
        // The identExpr "a" is in "return a + b" -- first identExpr matching "a".
        val identExpr = findIdentExpr("a", skip = 0)!!
        val ref = identExpr.reference
        assertNotNull("a identExpr should have a reference", ref)

        val resolved = ref!!.resolve()
        assertNotNull("a reference should resolve", resolved)
        assertInstanceOf(resolved, YarParam::class.java)
        assertEquals("a", (resolved as YarParam).name)
    }

    fun testStructNameResolvesToDeclaration() {
        myFixture.configureByText(
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
        val file = myFixture.file
        val structLit = PsiTreeUtil.findChildOfType(file, YarStructLiteralExpr::class.java)
        assertNotNull("Should find a struct literal", structLit)

        val ref = structLit!!.reference
        assertNotNull("Struct literal should have a reference for type name", ref)

        val resolved = ref!!.resolve()
        assertNotNull("Point reference should resolve", resolved)
        assertInstanceOf(resolved, YarStructDecl::class.java)
        assertEquals("Point", (resolved as YarStructDecl).name)
    }

    fun testDeclarationNameHasNoReference() {
        myFixture.configureByText(
            "test.yar",
            """
            package main
            fn greet() void {
            }
            """.trimIndent()
        )
        val file = myFixture.file
        val funcDecl = PsiTreeUtil.findChildOfType(file, YarFunctionDecl::class.java)!!
        val nameIdent = funcDecl.nameIdentifier!!
        val ref = YarReference.create(nameIdent)
        assertNull("Declaration name should not produce a reference", ref)
    }

    fun testFieldInitNameHasNoReference() {
        myFixture.configureByText(
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
        val file = myFixture.file
        val fieldInit = PsiTreeUtil.findChildOfType(
            PsiTreeUtil.findChildOfType(file, YarStructLiteralExpr::class.java),
            YarFieldInit::class.java
        )!!
        val nameIdent = fieldInit.firstChild
        val ref = YarReference.create(nameIdent)
        assertNull("Field init name should not have a reference", ref)
    }

    fun testForLoopVariableResolves() {
        myFixture.configureByText(
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
        // "i" in the short decl is the name of YarShortDeclStmt inside forInit, not an identExpr.
        // The identExpr "i" is in "i < 10" -- first identExpr matching "i".
        val identExpr = findIdentExpr("i", skip = 0)!!
        val ref = identExpr.reference
        assertNotNull("i identExpr should have a reference", ref)

        val resolved = ref!!.resolve()
        assertNotNull("i reference should resolve", resolved)
        assertInstanceOf(resolved, YarShortDeclStmt::class.java)
    }

    fun testCrossPackageFunctionCallViaCaretRef() {
        myFixture.addFileToProject(
            "lib/lib.yar",
            """
            package lib
            pub fn greet(name str) str {
                return name
            }
            """.trimIndent()
        )
        // <caret> marks cursor position -- this is what the IDE uses for Ctrl+Click
        myFixture.configureByText(
            "main.yar",
            """
            package main
            import "lib"
            fn main() i32 {
                lib.gre<caret>et("hello")
                return 0
            }
            """.trimIndent()
        )
        val ref = myFixture.getReferenceAtCaretPosition()
        assertNotNull("Ctrl+Click on greet should find a reference", ref)

        val resolved = ref!!.resolve()
        assertNotNull("greet should resolve to lib function", resolved)
        assertInstanceOf(resolved, YarFunctionDecl::class.java)
        assertEquals("greet", (resolved as YarFunctionDecl).name)
    }

    fun testCrossPackageStructLiteralViaCaretRef() {
        myFixture.addFileToProject(
            "lib/lib.yar",
            """
            package lib
            pub struct User {
                name str
            }
            """.trimIndent()
        )
        myFixture.configureByText(
            "main.yar",
            """
            package main
            import "lib"
            fn main() i32 {
                u := lib.Us<caret>er{name: "ada"}
                return 0
            }
            """.trimIndent()
        )
        val ref = myFixture.getReferenceAtCaretPosition()
        assertNotNull("Ctrl+Click on User should find a reference", ref)

        val resolved = ref!!.resolve()
        assertNotNull("lib.User should resolve to User struct", resolved)
        assertInstanceOf(resolved, YarStructDecl::class.java)
        assertEquals("User", (resolved as YarStructDecl).name)
    }

    fun testSameFileFunctionCallViaCaretRef() {
        myFixture.configureByText(
            "test.yar",
            """
            package main
            fn greet(name str) void {
                print(name)
            }
            fn main() i32 {
                gre<caret>et("hello")
                return 0
            }
            """.trimIndent()
        )
        val ref = myFixture.getReferenceAtCaretPosition()
        assertNotNull("Ctrl+Click on greet should find a reference", ref)

        val resolved = ref!!.resolve()
        assertNotNull("greet should resolve", resolved)
        assertInstanceOf(resolved, YarFunctionDecl::class.java)
        assertEquals("greet", (resolved as YarFunctionDecl).name)
    }

    fun testNonPackageDotAccessHasNoReference() {
        myFixture.configureByText(
            "test.yar",
            """
            package main
            struct User {
                name str
            }
            fn main() i32 {
                u := User{name: "ada"}
                print(u.na<caret>me)
                return 0
            }
            """.trimIndent()
        )
        val ref = myFixture.getReferenceAtCaretPosition()
        // Field access on local variable should NOT resolve (no type inference)
        if (ref != null) {
            assertNull("Field access should not resolve", ref.resolve())
        }
    }

    // --- Struct go-to-definition tests ---

    fun testStructInLiteralSameFile() {
        myFixture.configureByText(
            "test.yar",
            """
            package main
            struct Point {
                x i32
                y i32
            }
            fn main() i32 {
                p := Po<caret>int{x: 1, y: 2}
                return 0
            }
            """.trimIndent()
        )
        val ref = myFixture.getReferenceAtCaretPosition()
        assertNotNull("Ctrl+Click on Point in literal should find reference", ref)
        val resolved = ref!!.resolve()
        assertNotNull("Point should resolve", resolved)
        assertInstanceOf(resolved, YarStructDecl::class.java)
        assertEquals("Point", (resolved as YarStructDecl).name)
    }

    fun testStructInParamType() {
        myFixture.configureByText(
            "test.yar",
            """
            package main
            struct User {
                name str
            }
            fn greet(u Us<caret>er) void {
            }
            """.trimIndent()
        )
        val ref = myFixture.getReferenceAtCaretPosition()
        assertNotNull("Ctrl+Click on User in param type should find reference", ref)
        val resolved = ref!!.resolve()
        assertNotNull("User should resolve", resolved)
        assertInstanceOf(resolved, YarStructDecl::class.java)
        assertEquals("User", (resolved as YarStructDecl).name)
    }

    fun testStructInReturnType() {
        myFixture.configureByText(
            "test.yar",
            """
            package main
            struct User {
                name str
            }
            fn create() Us<caret>er {
                return User{name: "ada"}
            }
            """.trimIndent()
        )
        val ref = myFixture.getReferenceAtCaretPosition()
        assertNotNull("Ctrl+Click on User in return type should find reference", ref)
        val resolved = ref!!.resolve()
        assertNotNull("User should resolve", resolved)
        assertInstanceOf(resolved, YarStructDecl::class.java)
    }

    fun testStructInVarDecl() {
        myFixture.configureByText(
            "test.yar",
            """
            package main
            struct User {
                name str
            }
            fn main() i32 {
                var u Us<caret>er
                return 0
            }
            """.trimIndent()
        )
        val ref = myFixture.getReferenceAtCaretPosition()
        assertNotNull("Ctrl+Click on User in var decl should find reference", ref)
        val resolved = ref!!.resolve()
        assertNotNull("User should resolve", resolved)
        assertInstanceOf(resolved, YarStructDecl::class.java)
    }

    fun testCrossPackageStructInType() {
        myFixture.addFileToProject(
            "people/people.yar",
            """
            package people
            pub struct User {
                name str
            }
            """.trimIndent()
        )
        myFixture.configureByText(
            "main.yar",
            """
            package main
            import "people"
            fn greet(u people.Us<caret>er) void {
            }
            """.trimIndent()
        )
        val ref = myFixture.getReferenceAtCaretPosition()
        assertNotNull("Ctrl+Click on User in cross-package type should find reference", ref)
        val resolved = ref!!.resolve()
        assertNotNull("people.User should resolve", resolved)
        assertInstanceOf(resolved, YarStructDecl::class.java)
        assertEquals("User", (resolved as YarStructDecl).name)
    }

    fun testEnumInType() {
        myFixture.configureByText(
            "test.yar",
            """
            package main
            enum Color {
                Red
                Blue
            }
            fn pick(c Col<caret>or) void {
            }
            """.trimIndent()
        )
        val ref = myFixture.getReferenceAtCaretPosition()
        assertNotNull("Ctrl+Click on Color in type should find reference", ref)
        val resolved = ref!!.resolve()
        assertNotNull("Color should resolve", resolved)
        assertInstanceOf(resolved, YarEnumDecl::class.java)
        assertEquals("Color", (resolved as YarEnumDecl).name)
    }

    fun testInterfaceInType() {
        myFixture.configureByText(
            "test.yar",
            """
            package main
            interface Printer {
                print(s str) void
            }
            fn use(p Prin<caret>ter) void {
            }
            """.trimIndent()
        )
        val ref = myFixture.getReferenceAtCaretPosition()
        assertNotNull("Ctrl+Click on Printer in type should find reference", ref)
        val resolved = ref!!.resolve()
        assertNotNull("Printer should resolve", resolved)
        assertInstanceOf(resolved, YarInterfaceDecl::class.java)
        assertEquals("Printer", (resolved as YarInterfaceDecl).name)
    }

    private fun findIdentExpr(name: String, skip: Int): YarIdentExpr? {
        val file = myFixture.file
        val allIdentExprs = PsiTreeUtil.collectElementsOfType(file, YarIdentExpr::class.java)
        var count = 0
        for (expr in allIdentExprs) {
            if (expr.text == name) {
                if (count == skip) return expr
                count++
            }
        }
        return null
    }
}
