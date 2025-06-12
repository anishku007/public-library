package com.org.booklibrary.service;

import com.org.booklibrary.entity.Book;
import com.org.booklibrary.entity.BorrowingRecord;
import com.org.booklibrary.entity.User;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class BorrowingRecordServiceTest {

    @Mock
    private BorrowingRecordRepository borrowingRecordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BorrowingRecordService borrowingRecordService;

    private Book book1;
    private User user1;
    private BorrowingRecord record1;
    private BorrowingRecord record2;

    @BeforeEach
    void setUp() {
        book1 = new Book("Title A", "Author A", "ISBN-001", 2000, 5, 3);
        book1.setId(1L);

        user1 = new User("user1", "user1@example.com");
        user1.setId(101L);

        record1 = new BorrowingRecord(book1, user1, LocalDate.now());
        record1.setId(1001L);

        record2 = new BorrowingRecord(book1, user1, LocalDate.now().minusDays(10));
        record2.setReturnDate(LocalDate.now().minusDays(5));
        record2.setId(1002L);
    }

    @Test
    void testGetAllBorrowingRecords() {
        when(borrowingRecordRepository.findAll()).thenReturn(Arrays.asList(record1, record2));
        List<BorrowingRecord> records = borrowingRecordService.getAllBorrowingRecords();
        assertNotNull(records);
        assertEquals(2, records.size());
        verify(borrowingRecordRepository, times(1)).findAll();
    }

    @Test
    void testGetBorrowingRecordsByUserFound() {
        when(userRepository.findById(101L)).thenReturn(Optional.of(user1));
        when(borrowingRecordRepository.findByUser(user1)).thenReturn(Arrays.asList(record1, record2));
        List<BorrowingRecord> records = borrowingRecordService.getBorrowingRecordsByUser(101L);
        assertNotNull(records);
        assertEquals(2, records.size());
        verify(userRepository, times(1)).findById(101L);
        verify(borrowingRecordRepository, times(1)).findByUser(user1);
    }

    @Test
    void testGetBorrowingRecordsByBookFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(borrowingRecordRepository.findByBook(book1)).thenReturn(Arrays.asList(record1, record2));
        List<BorrowingRecord> records = borrowingRecordService.getBorrowingRecordsByBook(1L);
        assertNotNull(records);
        assertEquals(2, records.size());
        verify(bookRepository, times(1)).findById(1L);
        verify(borrowingRecordRepository, times(1)).findByBook(book1);
    }

}
