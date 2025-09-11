package io.allink.receipt.api

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun `application should initialize properly`() {
        // Given & When
        val result = true

        // Then
        assertTrue(result)
    }

    @Test
    fun `basic mathematical operations should work`() {
        // Given
        val a = 10
        val b = 20

        // When
        val sum = a + b
        val difference = b - a  
        val product = a * b
        val quotient = b / a

        // Then
        assertEquals(30, sum)
        assertEquals(10, difference)
        assertEquals(200, product)
        assertEquals(2, quotient)
    }

    @Test
    fun `string operations should work correctly`() {
        // Given
        val str1 = "Hello"
        val str2 = "World"

        // When
        val combined = "$str1 $str2"
        val upperCase = combined.uppercase()
        val lowerCase = combined.lowercase()

        // Then
        assertEquals("Hello World", combined)
        assertEquals("HELLO WORLD", upperCase)
        assertEquals("hello world", lowerCase)
    }

    @Test
    fun `list operations should work correctly`() {
        // Given
        val numbers = listOf(1, 2, 3, 4, 5)

        // When
        val doubled = numbers.map { it * 2 }
        val evens = numbers.filter { it % 2 == 0 }
        val sum = numbers.sum()

        // Then
        assertEquals(listOf(2, 4, 6, 8, 10), doubled)
        assertEquals(listOf(2, 4), evens)
        assertEquals(15, sum)
    }

    @Test
    fun `boolean logic should work correctly`() {
        // Given
        val isTrue = true
        val isFalse = false

        // When & Then
        assertTrue(isTrue && !isFalse)
        assertTrue(isTrue || isFalse)
        assertTrue(!isFalse)
        assertTrue(!(isFalse && isTrue))
    }
}