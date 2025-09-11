package io.allink.receipt.api.domain

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PageModelTest {

  @Test
  fun `Page should be created with default values`() {
    // Given & When
    val page = Page()
    
    // Then
    assertEquals(1, page.page)
    assertEquals(10, page.pageSize)
  }
  
  @Test
  fun `Page should be created with custom values`() {
    // Given & When
    val page = Page(page = 3, pageSize = 25)
    
    // Then
    assertEquals(3, page.page)
    assertEquals(25, page.pageSize)
  }
  
  @Test
  fun `Page should handle boundary values`() {
    // Given & When
    val minPage = Page(page = 1, pageSize = 1)
    val largePage = Page(page = 100, pageSize = 100)
    
    // Then
    assertEquals(1, minPage.page)
    assertEquals(1, minPage.pageSize)
    assertEquals(100, largePage.page)
    assertEquals(100, largePage.pageSize)
  }
  
  @Test
  fun `PagedResult should be created correctly`() {
    // Given
    val items = listOf("item1", "item2", "item3")
    val totalCount = 10
    val currentPage = 2
    val totalPages = 4
    
    // When
    val pagedResult = PagedResult(
      items = items,
      totalCount = totalCount,
      currentPage = currentPage,
      totalPages = totalPages
    )
    
    // Then
    assertEquals(items, pagedResult.items)
    assertEquals(totalCount, pagedResult.totalCount)
    assertEquals(currentPage, pagedResult.currentPage)
    assertEquals(totalPages, pagedResult.totalPages)
  }
  
  @Test
  fun `PagedResult should handle empty results`() {
    // Given
    val emptyItems = emptyList<String>()
    
    // When
    val pagedResult = PagedResult(
      items = emptyItems,
      totalCount = 0,
      currentPage = 1,
      totalPages = 0
    )
    
    // Then
    assertTrue(pagedResult.items.isEmpty())
    assertEquals(0, pagedResult.totalCount)
    assertEquals(1, pagedResult.currentPage)
    assertEquals(0, pagedResult.totalPages)
  }
  
  @Test
  fun `Sorter should be created with field and direction`() {
    // Given & When
    val ascSorter = Sorter(field = "name", direction = "ASC")
    val descSorter = Sorter(field = "createdAt", direction = "DESC")
    
    // Then
    assertEquals("name", ascSorter.field)
    assertEquals("ASC", ascSorter.direction)
    assertEquals("createdAt", descSorter.field)
    assertEquals("DESC", descSorter.direction)
  }
  
  @Test
  fun `Sorter should handle different field types`() {
    // Given & When
    val dateSorter = Sorter(field = "regDate", direction = "DESC")
    val idSorter = Sorter(field = "id", direction = "ASC")
    val nameSorter = Sorter(field = "storeName", direction = "ASC")
    
    // Then
    assertEquals("regDate", dateSorter.field)
    assertEquals("DESC", dateSorter.direction)
    assertEquals("id", idSorter.field)
    assertEquals("ASC", idSorter.direction)
    assertEquals("storeName", nameSorter.field)
    assertEquals("ASC", nameSorter.direction)
  }
  
  @Test
  fun `PeriodFilter should handle date ranges`() {
    // Given
    val from = java.time.LocalDateTime.now().minusDays(30)
    val to = java.time.LocalDateTime.now()
    
    // When
    val periodFilter = PeriodFilter(from = from, to = to)
    
    // Then
    assertEquals(from, periodFilter.from)
    assertEquals(to, periodFilter.to)
    assertTrue(periodFilter.from.isBefore(periodFilter.to))
  }
}