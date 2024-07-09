package com.kako351.exampleksp

import com.kako351.exampleksp.processer.MyAnnotation
import org.junit.Assert
import org.junit.Test

@MyAnnotation
class ExampleTest {
    @Test
    fun test() {
        println("Hello, KSP!")
    }

    fun testMiss() {
        println("Hello, KSP!")
    }

}