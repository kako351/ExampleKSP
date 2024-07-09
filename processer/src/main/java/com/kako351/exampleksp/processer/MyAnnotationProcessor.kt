package com.kako351.exampleksp.processer

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.validate

class MyAnnotationProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val testClasses = resolver.getSymbolsWithAnnotation("org.junit.jupiter.api.Test")
            .filterIsInstance<KSClassDeclaration>()

        for (testClass in testClasses) {
            val testMethods = testClass.getAllFunctions()
                .filter { it.isTestFunction() }

            for (testMethod in testMethods) {
                if (!testMethod.annotations.filter { it.shortName.getShortName().contains("Test") }.none()) {
                    logger.error("@Test annotation is missing on test method: ${testMethod.qualifiedName?.asString()}", testMethod)
                }
            }
        }

        val symbols = resolver.getSymbolsWithAnnotation("com.kako351.exampleksp.processer.MyAnnotation")
        symbols.filterIsInstance<KSClassDeclaration>().forEach { classDecl ->
            val allTestChecked = classDecl.getAllFunctions()
                .filter { it.isTestFunction() }
                .filter {
                    it.annotations.filter { it.shortName.getShortName().contains("Test") }.none()
                }.toList().isEmpty()


            val code = """
                package ${classDecl.packageName.asString()}

                import org.junit.Assert
                import org.junit.Test

                class ${classDecl.simpleName.asString()}Action() {
                    @Test
                    fun notAssertionTestCheck() {
                        Assert.assertTrue(${allTestChecked})
                    }
                }
            """.trimIndent()

            val file = codeGenerator.createNewFile(
                Dependencies(true, classDecl.containingFile!!),
                classDecl.packageName.asString(),
                "${classDecl.simpleName.asString()}Action"
            )

            file.write(code.toByteArray())
            file.close()
        }

        return emptyList()
    }

    private fun KSFunctionDeclaration.isTestFunction(): Boolean {
        // テストメソッドの判定ロジックを実装する
        // 例: メソッド名が "test" で始まるかどうか、または特定のアノテーションが付いているかどうか
        return simpleName.asString().startsWith("test")
    }

    override fun finish() {
        // Finalization code if needed
    }

    override fun onError() {
        // Error handling code
    }
}
