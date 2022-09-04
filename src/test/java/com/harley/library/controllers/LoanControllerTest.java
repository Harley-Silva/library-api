package com.harley.library.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harley.library.dtos.LoanDTO;
import com.harley.library.entities.Book;
import com.harley.library.entities.Loan;
import com.harley.library.exceptions.BusinessException;
import com.harley.library.services.BookService;
import com.harley.library.services.LoanService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {
    static final String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;

    @Test
    @DisplayName("Must create a loan when call the create method passing a loanDTO")
    void givenValidLoanDTO_WhenCallCreate_ThenReturnCreatedLoan() throws Exception {
        // Given
        LoanDTO loanDTO = LoanDTO.builder().isbn("123").customer("Harley").build();
        String json = new ObjectMapper().writeValueAsString(loanDTO);

        Book book = Book.builder().id(1l).isbn("123").build();
        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book));
        Loan loan =  Loan.builder().id(1l).customer("Harley").book(book).date(LocalDate.now()).build();
        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

        // When
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        // Then
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string("1"));
    }

    @Test
    @DisplayName("Must throw an exception when calling the create method passing an invalid isbn")
    void givenInvalidIsbn_WhenCallCreate_ThenThrowAnException() throws Exception {
        // Given
        LoanDTO loanDTO = LoanDTO.builder().isbn("123").customer("Harley").build();
        String json = new ObjectMapper().writeValueAsString(loanDTO);

        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.empty());

        // When
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        // Then
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("errors[0]").value("Book not found for passed isbn"));
    }

    @Test
    @DisplayName("Must throw an exception when calling the create method passing an isbn of borrowed book")
    void givenAnIsbnOfBorrowed_WhenCallCreate_ThenThrowAnException() throws Exception {
        // Given
        LoanDTO loanDTO = LoanDTO.builder().isbn("123").customer("Harley").build();
        String json = new ObjectMapper().writeValueAsString(loanDTO);

        Book book = Book.builder().id(1l).isbn("123").build();
        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book));
        BDDMockito.given(loanService.save(Mockito.any())).willThrow(new BusinessException("Book already borrowed"));

        // When
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        // Then
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("errors[0]").value("Book already borrowed"));
    }
}