package com.org.booklibrary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.booklibrary.entity.Book;
import com.org.booklibrary.entity.BorrowingRecord;
import com.org.booklibrary.entity.User;
import com.org.booklibrary.exception.ResourceNotFoundException;
import com.org.booklibrary.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    private Book book1;
    private Book book2;
    private User user1;
    private BorrowingRecord borrowingRecord1;

    @BeforeEach
    void setUp() {
        book1 = new Book("Title A", "Author A", "ISBN-001", 2000, 5, 3);
        book1.setId(1L);

        book2 = new Book("Title B", "Author B", "ISBN-002", 2010, 1, 0);
        book2.setId(2L);

        user1 = new User("user1", "user1@example.com");
        user1.setId(101L);

        borrowingRecord1 = new BorrowingRecord(book1, user1, LocalDate.now());
        borrowingRecord1.setId(1001L);
    }

    @Test
    void testGetAllBooks() throws Exception {
        List<Book> allBooks = Arrays.asList(book1, book2);
        when(bookService.getAllBooks()).thenReturn(allBooks);

        mockMvc.perform(get("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(allBooks.size()))
                .andExpect(jsonPath("$[0].title").value("Title A"));

        verify(bookService, times(1)).getAllBooks();
        verify(bookService, never()).searchBooks(anyString(), anyString(), anyString()); // Should not call search if no params
    }

    @Test
    void testSearchBooksByTitle() throws Exception {
        when(bookService.searchBooks("Title A", null, null)).thenReturn(Collections.singletonList(book1));

        mockMvc.perform(get("/api/v1/books?title=Title A")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].title").value("Title A"));

        verify(bookService, times(1)).searchBooks("Title A", null, null);
        verify(bookService, never()).getAllBooks();
    }

    @Test
    void testSearchBooksByAuthor() throws Exception {
        when(bookService.searchBooks(null, "Author A", null)).thenReturn(Collections.singletonList(book1));

        mockMvc.perform(get("/api/v1/books?author=Author A")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].author").value("Author A"));

        verify(bookService, times(1)).searchBooks(null, "Author A", null);
        verify(bookService, never()).getAllBooks();
    }

    @Test
    void testSearchBooksByISBN() throws Exception {
        when(bookService.searchBooks(null, null, "ISBN-001")).thenReturn(Collections.singletonList(book1));

        mockMvc.perform(get("/api/v1/books?isbn=ISBN-001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].isbn").value("ISBN-001"));

        verify(bookService, times(1)).searchBooks(null, null, "ISBN-001");
        verify(bookService, never()).getAllBooks();
    }

    @Test
    void testGetBookByIdFound() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(book1);

        mockMvc.perform(get("/api/v1/books/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(book1.getTitle()));

        verify(bookService, times(1)).getBookById(1L);
    }

    @Test
    void testGetBookByIdNotFound() throws Exception {
        when(bookService.getBookById(99L)).thenThrow(new ResourceNotFoundException("Book not found with id: 99"));

        mockMvc.perform(get("/api/v1/books/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found with id: 99"));

        verify(bookService, times(1)).getBookById(99L);
    }

    @Test
    void testAddBookSuccess() throws Exception {
        Book newBook = new Book("New Title", "New Author", "ISBN-003", 2020, 10, 10);
        Book savedBook = new Book("New Title", "New Author", "ISBN-003", 2020, 10, 10);
        savedBook.setId(3L);

        when(bookService.addBook(any(Book.class))).thenReturn(savedBook);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.title").value("New Title"));

        verify(bookService, times(1)).addBook(any(Book.class));
    }

    @Test
    void testUpdateBookSuccess() throws Exception {
        Book updatedDetails = new Book("Updated Title A", "Author A", "ISBN-001", 2000, 5, 2);
        when(bookService.updateBook(eq(1L), any(Book.class))).thenReturn(updatedDetails);

        mockMvc.perform(put("/api/v1/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title A"));

        verify(bookService, times(1)).updateBook(eq(1L), any(Book.class));
    }

    @Test
    void testUpdateBookNotFound() throws Exception {
        Book updatedDetails = new Book("Non Existent", "Author", "ISBN-XXX", 2000, 1, 1);
        when(bookService.updateBook(eq(99L), any(Book.class))).thenThrow(new ResourceNotFoundException("Book not found with id: 99"));

        mockMvc.perform(put("/api/v1/books/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found with id: 99"));

        verify(bookService, times(1)).updateBook(eq(99L), any(Book.class));
    }

    @Test
    void testDeleteBookSuccess() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/v1/books/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteBook(1L);
    }

    @Test
    void testDeleteBookNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Book not found with id: 99")).when(bookService).deleteBook(99L);

        mockMvc.perform(delete("/api/v1/books/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found with id: 99"));

        verify(bookService, times(1)).deleteBook(99L);
    }

    @Test
    void testBorrowBookSuccess() throws Exception {
        when(bookService.borrowBook(1L, 101L)).thenReturn(borrowingRecord1);

        mockMvc.perform(post("/api/v1/books/1/borrow/101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1001L));

        verify(bookService, times(1)).borrowBook(1L, 101L);
    }

    @Test
    void testReturnBookSuccess() throws Exception {
        BorrowingRecord returnedRecord = new BorrowingRecord(book1, user1, LocalDate.now().minusDays(5));
        returnedRecord.setId(1001L);
        returnedRecord.setReturnDate(LocalDate.now());

        when(bookService.returnBook(1001L)).thenReturn(returnedRecord);

        mockMvc.perform(post("/api/v1/books/1001/return")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1001L))
                .andExpect(jsonPath("$.returnDate").exists());

        verify(bookService, times(1)).returnBook(1001L);
    }

    @Test
    void testReturnBookAlreadyReturned() throws Exception {
        when(bookService.returnBook(1001L))
                .thenThrow(new IllegalStateException("This book has already been returned."));

        mockMvc.perform(post("/api/v1/books/1001/return")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("This book has already been returned."));

        verify(bookService, times(1)).returnBook(1001L);
    }
}
