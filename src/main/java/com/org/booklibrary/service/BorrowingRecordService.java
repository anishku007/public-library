package com.org.booklibrary.service;

import com.org.booklibrary.entity.Book;
import com.org.booklibrary.entity.BorrowingRecord;
import com.org.booklibrary.entity.User;
import com.org.booklibrary.exception.ResourceNotFoundException;
import com.org.booklibrary.repository.BookRepository;
import com.org.booklibrary.repository.BorrowingRecordRepository;
import com.org.booklibrary.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BorrowingRecordService {

    private final BorrowingRecordRepository borrowingRecordRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Autowired
    public BorrowingRecordService(BorrowingRecordRepository borrowingRecordRepository, UserRepository userRepository, BookRepository bookRepository) {
        this.borrowingRecordRepository = borrowingRecordRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    public List<BorrowingRecord> getAllBorrowingRecords() {
        return borrowingRecordRepository.findAll();
    }

    public List<BorrowingRecord> getBorrowingRecordsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return borrowingRecordRepository.findByUser(user);
    }

    public List<BorrowingRecord> getBorrowingRecordsByBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        return borrowingRecordRepository.findByBook(book);
    }
}
