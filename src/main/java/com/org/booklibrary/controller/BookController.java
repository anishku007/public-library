package com.org.booklibrary.controller;

import com.org.booklibrary.entity.Book;
import com.org.booklibrary.entity.BorrowingRecord;
import com.org.booklibrary.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Developed By Anish Kumar
 * REST Controller for managing Book entities and borrowing/returning operations.
 */
@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    @Operation(summary = "Get all books or search books",
            description = "Retrieves a list of all books or searches for books by title, author, or ISBN.")
    public ResponseEntity<List<Book>> getBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String isbn) {

        if (title != null || author != null || isbn != null) {
            List<Book> books = bookService.searchBooks(title, author, isbn);
            return ResponseEntity.ok(books);
        } else {
            List<Book> books = bookService.getAllBooks();
            return ResponseEntity.ok(books);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a book by ID",
            description = "Retrieves a single book by its unique identifier.")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @PostMapping
    @Operation(summary = "Add a new book",
            description = "Creates a new book entry in the library.")
    public ResponseEntity<Book> addBook(@Valid @RequestBody Book book) {
        Book newBook = bookService.addBook(book);
        return new ResponseEntity<>(newBook, HttpStatus.CREATED);
    }


    @PutMapping("/{id}")
    @Operation(summary = "Update an existing book",
            description = "Updates the details of an existing book by its ID.")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @Valid @RequestBody Book bookDetails) {
        Book updatedBook = bookService.updateBook(id, bookDetails);
        return ResponseEntity.ok(updatedBook);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book",
            description = "Deletes a book from the library by its ID.")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{bookId}/borrow/{userId}")
    @Operation(summary = "Borrow a book",
            description = "Allows a user to borrow a specific book from the available copies.")
    public ResponseEntity<BorrowingRecord> borrowBook(@PathVariable Long bookId, @PathVariable Long userId) {
        BorrowingRecord record = bookService.borrowBook(bookId, userId);
        return new ResponseEntity<>(record, HttpStatus.CREATED);
    }

    @PostMapping("/{recordId}/return")
    @Operation(summary = "Return a borrowed book",
            description = "Marks a borrowed book as returned using its borrowing record ID.")
    public ResponseEntity<BorrowingRecord> returnBook(@PathVariable Long recordId) {
        BorrowingRecord updatedRecord = bookService.returnBook(recordId);
        return ResponseEntity.ok(updatedRecord);
    }
}
