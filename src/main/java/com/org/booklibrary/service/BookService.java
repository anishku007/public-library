package com.org.booklibrary.service;

import com.org.booklibrary.entity.Book;
import com.org.booklibrary.entity.BorrowingRecord;
import com.org.booklibrary.entity.User;
import com.org.booklibrary.exception.BookUnavailableException;
import com.org.booklibrary.exception.ResourceNotFoundException;
import com.org.booklibrary.repository.BookRepository;
import com.org.booklibrary.repository.BorrowingRecordRepository;
import com.org.booklibrary.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BookService {
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;

    @Autowired
    public BookService(BookRepository bookRepository, UserRepository userRepository, BorrowingRecordRepository borrowingRecordRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.borrowingRecordRepository = borrowingRecordRepository;
    }

    /**
     * Retrieves all books.
     * @return A list of all books.
     */
    public List<Book> getAllBooks() {
        log.debug("Attempting to retrieve all books.");
        List<Book> books = bookRepository.findAll();
        log.info("Retrieved {} books.", books.size());
        return books;
    }

    public Book getBookById(Long id) {
        log.debug("Attempting to retrieve book with ID: {}", id);
        return bookRepository.findById(id)
                .map(book -> {
                    log.info("Book with ID: {} found - Title: {}", id, book.getTitle());
                    return book;
                })
                .orElseThrow(() -> {
                    log.warn("Book not found with ID: {}", id);
                    return new ResourceNotFoundException("Book not found with id: " + id);
                });
    }

    public Book addBook(Book book) {
        log.info("Attempting to add new book: {}", book.getTitle());
        if (book.getAvailableCopies() == null) {
            book.setAvailableCopies(book.getTotalCopies());
            log.debug("Available copies not specified, setting to total copies: {}", book.getTotalCopies());
        }
        Book savedBook = bookRepository.save(book);
        log.info("Book added successfully with ID: {} and Title: {}", savedBook.getId(), savedBook.getTitle());
        return savedBook;
    }

    public Book updateBook(Long id, Book bookDetails) {
        log.info("Attempting to update book with ID: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Book not found for update with ID: {}", id);
                    return new ResourceNotFoundException("Book not found with id: " + id);
                });

        log.debug("Found book for update: {}. Applying updates from: {}", book.getTitle(), bookDetails.getTitle());
        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setIsbn(bookDetails.getIsbn());
        book.setPublicationYear(bookDetails.getPublicationYear());
        book.setTotalCopies(bookDetails.getTotalCopies());
        if (bookDetails.getAvailableCopies() != null) {
            book.setAvailableCopies(Math.min(bookDetails.getAvailableCopies(), bookDetails.getTotalCopies()));
        } else {
            book.setAvailableCopies(bookDetails.getTotalCopies());
        }
        Book updatedBook = bookRepository.save(book);
        log.info("Book with ID: {} updated successfully.", updatedBook.getId());
        return updatedBook;
    }

    public void deleteBook(Long id) {
        log.info("Attempting to delete book with ID: {}", id);
        if (!bookRepository.existsById(id)) {
            log.warn("Book not found for deletion with ID: {}", id);
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
        log.info("Book with ID: {} deleted successfully.", id);
    }

    @Transactional
    public BorrowingRecord borrowBook(Long bookId, Long userId) {
        log.info("Attempting to borrow book (ID: {}) by user (ID: {}).", bookId, userId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if the book is available
        if (book.getAvailableCopies() <= 0) {
            throw new BookUnavailableException("No copies of the book '" + book.getTitle() + "' are currently available.");
        }

        // Check if the user already has this book borrowed and not returned
        Optional<BorrowingRecord> existingRecord = borrowingRecordRepository.findByUserAndBookAndReturnDateIsNull(user, book);
        if (existingRecord.isPresent()) {
            throw new BookUnavailableException("User '" + user.getUsername() + "' has already borrowed '" + book.getTitle() + "' and has not returned it yet.");
        }

        // Decrease available copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book); // Save the updated book entity

        // Create a new borrowing record
        BorrowingRecord borrowingRecord = new BorrowingRecord(book, user, LocalDate.now());
        log.info("Borrowing record created for Book '{}' (ID: {}) by User '{}' (ID: {})",
                book.getTitle(), bookId, user.getUsername(), userId);
        return borrowingRecordRepository.save(borrowingRecord);
    }

    @Transactional
    public BorrowingRecord returnBook(Long recordId) {
        log.info("Attempting to return book using borrowing record ID: {}", recordId);
        BorrowingRecord record = borrowingRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing record not found with id: " + recordId));

        if (record.getReturnDate() != null) {
            throw new IllegalStateException("This book has already been returned.");
        }
        Book book = record.getBook();
        if (book.getAvailableCopies() < book.getTotalCopies()) {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
        }
        bookRepository.save(book);

        record.setReturnDate(LocalDate.now());
        return borrowingRecordRepository.save(record);
    }

    public List<Book> searchBooks(String title, String author, String isbn) {
        log.info("Searching books with title: '{}', author: '{}', isbn: '{}'", title, author, isbn);
        List<Book> books;
        if (isbn != null && !isbn.isBlank()) {
            books = bookRepository.findByIsbn(isbn);
            log.debug("Found {} books by ISBN: {}", books.size(), isbn);
        } else if (title != null && !title.isBlank()) {
            books = bookRepository.findByTitleContainingIgnoreCase(title);
            log.debug("Found {} books by title: {}", books.size(), title);
        } else if (author != null && !author.isBlank()) {
            books = bookRepository.findByAuthorContainingIgnoreCase(author);
            log.debug("Found {} books by author: {}", books.size(), author);
        } else {
            books = bookRepository.findAll();
            log.debug("No specific search criteria, returning all {} books.", books.size());
        }
        return books;
    }
}
