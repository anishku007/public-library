package com.org.booklibrary.repository;

import com.org.booklibrary.entity.Book;
import com.org.booklibrary.entity.BorrowingRecord;
import com.org.booklibrary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {

    List<BorrowingRecord> findByUser(User user);

    List<BorrowingRecord> findByBook(Book book);

    Optional<BorrowingRecord> findByUserAndBookAndReturnDateIsNull(User user, Book book);
}
