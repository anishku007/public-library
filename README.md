# Public Books  library application
# Book Library Spring Boot Application
# This is a simple Spring Boot application that simulates a book library system.
# It allows for managing books and users, as well as tracking borrowing and returning of books.
# The API is designed to be RESTful and includes versioning and Swagger UI for documentation.

# Technologies Used
Java 17
Spring Boot 3.2.5
Spring Data JPA
Hibernate
H2 Database (in-memory)
Maven
Lombok (implicitly used for boilerplate reduction, though not explicitly in the provided entities)
SpringDoc OpenAPI UI (for Swagger)
Docker
JUnit 5 & Mockito (for unit testing)


# Swagger-link
http://localhost:8080/swagger-ui/index.html

http://localhost:8080/swagger-ui/index.html#/book-controller/getBooks

# H2-DB Details
# Use this info on the H2 login screen:
# http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:bookLibrary
User Name: sa
Password: (leave blank unless changed)

# Initial data setup for H2 database

-- Insert some sample Books
INSERT INTO book (title, author, isbn, publication_year, total_copies, available_copies) VALUES
('The Hitchhiker''s Guide to the Galaxy', 'Douglas Adams', '978-0345391803', 1979, 5, 5),
('1984', 'George Orwell', '978-0451524935', 1949, 3, 3),
('Pride and Prejudice', 'Jane Austen', '978-0141439518', 1813, 4, 4),
('To Kill a Mockingbird', 'Harper Lee', '978-0446310789', 1960, 2, 2);

-- Insert some sample Users
INSERT INTO library_user (username, email) VALUES
('john_doe', 'john.doe@example.com'),
('jane_smith', 'jane.smith@example.com'),
('bob_johnson', 'bob.johnson@example.com');

# query
select * from BOOK
Select * from LIBRARY_USER
select * from BORROWING_RECORD

