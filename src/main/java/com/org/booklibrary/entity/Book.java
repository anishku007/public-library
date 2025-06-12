package com.org.booklibrary.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is mandatory")
    private String title;

    @NotBlank(message = "Author is mandatory")
    private String author;

    @NotBlank(message = "ISBN is mandatory")
    private String isbn;

    @NotNull(message = "Publication year is mandatory")
    @Min(value = 1000, message = "Publication year must be a valid year")
    private Integer publicationYear;

    @NotNull(message = "Total copies is mandatory")
    @Min(value = 0, message = "Total copies cannot be negative")
    private Integer totalCopies;

    @NotNull(message = "Available copies is mandatory")
    @Min(value = 0, message = "Available copies cannot be negative")
    private Integer availableCopies;

    public Book(String s, String author, String s1, int i, int i1, int i2) {
        this.title=s;
        this.author=author;
        this.isbn=s1;
        this.publicationYear=i;
        this.totalCopies=i1;
        this.availableCopies=i2;
    }
}
