package com.org.booklibrary.service;

import com.org.booklibrary.entity.Book;
import com.org.booklibrary.entity.BorrowingRecord;
import com.org.booklibrary.entity.User;
import com.org.booklibrary.exception.BookUnavailableException;
import com.org.booklibrary.exception.ResourceNotFoundException;
import com.org.booklibrary.repository.BookRepository;
import com.org.booklibrary.repository.BorrowingRecordRepository;
import com.org.booklibrary.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BorrowingRecordRepository borrowingRecordRepository;

    @InjectMocks
    private BookService bookService;

    private Book book1;
    private Book book2;
    private User user1;
    private BorrowingRecord borrowingRecord1;

    @BeforeEach
    void setUp() {
        // Initialize sample data for tests
        book1 = new Book("Title A", "Author A", "ISBN-001", 2000, 5, 3);
        book1.setId(1L);

        book2 = new Book("Title B", "Author B", "ISBN-002", 2010, 1, 0); // Book with 0 available copies
        book2.setId(2L);

        user1 = new User("user1", "user1@example.com");
        user1.setId(101L);

        borrowingRecord1 = new BorrowingRecord(book1, user1, LocalDate.now());
        borrowingRecord1.setId(1001L);
    }

    @Test
    void testGetAllBooks() {
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));
        List<Book> books = bookService.getAllBooks();
        assertNotNull(books);
        assertEquals(2, books.size());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void testAddBook() {
        Book newBook = new Book("New Title", "New Author", "ISBN-003", 2020, 10, 10);
        when(bookRepository.save(any(Book.class))).thenReturn(newBook);
        Book addedBook = bookService.addBook(newBook);
        assertNotNull(addedBook);
        assertEquals("New Title", addedBook.getTitle());
        assertEquals(10, addedBook.getAvailableCopies()); // Should set availableCopies to totalCopies if null
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testUpdateBookFound() {
        Book updatedDetails = new Book("Updated Title", "Updated Author", "ISBN-001", 2000, 6, 4);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookRepository.save(any(Book.class))).thenReturn(book1); // Mock saving the updated book1

        Book updatedBook = bookService.updateBook(1L, updatedDetails);
        assertNotNull(updatedBook);
        assertEquals("Updated Title", updatedBook.getTitle());
        assertEquals(4, updatedBook.getAvailableCopies()); // Should reflect updated available copies
        assertEquals(6, updatedBook.getTotalCopies());
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).save(book1);
    }

    @Test
    void testUpdateBookNotFound() {
        Book updatedDetails = new Book("Non-existent", "Author", "ISBN-XXX", 2000, 1, 1);
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookService.updateBook(99L, updatedDetails));
        verify(bookRepository, times(1)).findById(99L);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testBorrowBookSuccess() {
        // Book with available copies
        book1.setAvailableCopies(3);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(userRepository.findById(101L)).thenReturn(Optional.of(user1));
        when(borrowingRecordRepository.findByUserAndBookAndReturnDateIsNull(user1, book1)).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(book1);
        when(borrowingRecordRepository.save(any(BorrowingRecord.class))).thenReturn(borrowingRecord1);

        BorrowingRecord record = bookService.borrowBook(1L, 101L);

        assertNotNull(record);
        assertEquals(book1.getId(), record.getBook().getId());
        assertEquals(user1.getId(), record.getUser().getId());
        assertEquals(LocalDate.now(), record.getBorrowDate());
        assertEquals(2, book1.getAvailableCopies()); // Available copies should decrease
        verify(bookRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(101L);
        verify(borrowingRecordRepository, times(1)).findByUserAndBookAndReturnDateIsNull(user1, book1);
        verify(bookRepository, times(1)).save(book1);
        verify(borrowingRecordRepository, times(1)).save(any(BorrowingRecord.class));
    }

    @Test
    void testBorrowBookUserNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.borrowBook(1L, 999L));

        verify(bookRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(999L);
        verify(bookRepository, never()).save(any(Book.class));
        verify(borrowingRecordRepository, never()).save(any(BorrowingRecord.class));
    }

    @Test
    void testBorrowBookAlreadyBorrowedByUser() {
        // Book with available copies
        book1.setAvailableCopies(3);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(userRepository.findById(101L)).thenReturn(Optional.of(user1));
        when(borrowingRecordRepository.findByUserAndBookAndReturnDateIsNull(user1, book1)).thenReturn(Optional.of(borrowingRecord1));

        assertThrows(BookUnavailableException.class, () -> bookService.borrowBook(1L, 101L));

        verify(bookRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(101L);
        verify(borrowingRecordRepository, times(1)).findByUserAndBookAndReturnDateIsNull(user1, book1);
        verify(bookRepository, never()).save(any(Book.class)); // Should not decrement if already borrowed
        verify(borrowingRecordRepository, never()).save(any(BorrowingRecord.class));
    }

    @Test
    void testReturnBookSuccess() {
        borrowingRecord1.setReturnDate(null); // Ensure it's not returned yet
        book1.setAvailableCopies(2); // Assume 2 copies were available before this return

        when(borrowingRecordRepository.findById(1001L)).thenReturn(Optional.of(borrowingRecord1));
        when(bookRepository.save(any(Book.class))).thenReturn(book1);
        when(borrowingRecordRepository.save(any(BorrowingRecord.class))).thenReturn(borrowingRecord1);

        BorrowingRecord returnedRecord = bookService.returnBook(1001L);

        assertNotNull(returnedRecord.getReturnDate());
        assertEquals(3, book1.getAvailableCopies()); // Available copies should increase
        verify(borrowingRecordRepository, times(1)).findById(1001L);
        verify(bookRepository, times(1)).save(book1);
        verify(borrowingRecordRepository, times(1)).save(borrowingRecord1);
    }

    @Test
    void testReturnBookRecordNotFound() {
        when(borrowingRecordRepository.findById(9999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookService.returnBook(9999L));
        verify(borrowingRecordRepository, times(1)).findById(9999L);
        verify(bookRepository, never()).save(any(Book.class));
        verify(borrowingRecordRepository, never()).save(any(BorrowingRecord.class));
    }

    @Test
    void testReturnBookAlreadyReturned() {
        borrowingRecord1.setReturnDate(LocalDate.now().minusDays(1)); // Already returned
        when(borrowingRecordRepository.findById(1001L)).thenReturn(Optional.of(borrowingRecord1));

        assertThrows(IllegalStateException.class, () -> bookService.returnBook(1001L));

        verify(borrowingRecordRepository, times(1)).findById(1001L);
        verify(bookRepository, never()).save(any(Book.class));
        verify(borrowingRecordRepository, never()).save(any(BorrowingRecord.class));
    }

    @Test
    void testSearchBooksByAuthor() {
        when(bookRepository.findByAuthorContainingIgnoreCase("author")).thenReturn(Arrays.asList(book1));
        List<Book> result = bookService.searchBooks(null, "author", null);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Author A", result.get(0).getAuthor());
        verify(bookRepository, times(1)).findByAuthorContainingIgnoreCase("author");
        verify(bookRepository, never()).findByTitleContainingIgnoreCase(anyString());
        verify(bookRepository, never()).findByIsbn(anyString());
        verify(bookRepository, never()).findAll();
    }

    @Test
    void testSearchBooksByIsbn() {
        when(bookRepository.findByIsbn("ISBN-001")).thenReturn(Arrays.asList(book1));
        List<Book> result = bookService.searchBooks(null, null, "ISBN-001");
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("ISBN-001", result.get(0).getIsbn());
        verify(bookRepository, times(1)).findByIsbn("ISBN-001");
        verify(bookRepository, never()).findByTitleContainingIgnoreCase(anyString());
        verify(bookRepository, never()).findByAuthorContainingIgnoreCase(anyString());
        verify(bookRepository, never()).findAll();
    }

}
