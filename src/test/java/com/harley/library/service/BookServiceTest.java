package com.harley.library.service;

import com.harley.library.entities.Book;
import com.harley.library.exceptions.BusinessException;
import com.harley.library.implementations.BookServiceImp;
import com.harley.library.respositories.BookRepository;
import com.harley.library.services.BookService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService bookService;
    @MockBean
    BookRepository bookRepository;

    @BeforeEach
    public void setUp() {
        this.bookService = new BookServiceImp(bookRepository);
    }

    @Test
    @DisplayName("Must save a book")
    public void saveBookTest() {
        //Scenary
        Book book = createValidBook();
        Book book2 = createValidBook();
        Mockito.when(bookRepository.existsByIsbn(Mockito.anyString())).thenReturn(false);
        Mockito.when(bookService.save(book)).thenReturn(book2);

        // Execution
        Book savedBook = bookService.save(book);

        // Verification
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("1234");
        assertThat(savedBook.getAuthor()).isEqualTo("Mary");
        assertThat(savedBook.getTitle()).isEqualTo("My Adventures");
    }

    @Test
    @DisplayName("Should throw an error when trying to register a book with an existing isbn.")
    public void shouldNotSaveABookWithDuplicatedIsbn() {
        // Scenary
        Book book =  createValidBook();
        Mockito.when(bookRepository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        // Execution
        Throwable throwble = Assertions.catchThrowable(() -> bookService.save(book));

        // Verification
        assertThat(throwble)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn already registered");
        Mockito.verify(bookRepository, Mockito.never()).save(book);

    }

    private Book createValidBook() {
        return Book.builder().title("My Adventures").author("Mary").isbn("1234").build();
    }
}