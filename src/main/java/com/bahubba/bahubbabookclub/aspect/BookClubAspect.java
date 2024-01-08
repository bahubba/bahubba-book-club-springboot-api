package com.bahubba.bahubbabookclub.aspect;

import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.service.BookClubService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class BookClubAspect {
    private final BookClubService bookClubService;

    @AfterReturning(
            pointcut =
                    "execution(com.bahubba.bahubbabookclub.model.dto.BookClubDTO com.bahubba.bahubbabookclub.service.*.*(..))",
            returning = "bookClubDTO")
    public void addPreSignedURL(JoinPoint joinPoint, @NotNull BookClubDTO bookClubDTO) {
        bookClubDTO.setImageURL(bookClubService.getPreSignedImageURL(
                bookClubDTO.isImageUploaded()
                        ? bookClubDTO.getName() + bookClubDTO.getImageExtension()
                        : "default.jpg"));
    }

    @AfterReturning(
            pointcut =
                    "execution(org.springframework.data.domain.Page<com.bahubba.bahubbabookclub.model.dto.BookClubDTO> com.bahubba.bahubbabookclub.service.*.*(..))",
            returning = "bookClubDTOs")
    public void addPreSignedURL(JoinPoint joinPoint, @NotNull Page<BookClubDTO> bookClubDTOs) {
        bookClubDTOs.forEach(bookClubDTO -> bookClubDTO.setImageURL(bookClubService.getPreSignedImageURL(
                bookClubDTO.isImageUploaded()
                        ? bookClubDTO.getName() + bookClubDTO.getImageExtension()
                        : "default.jpg")));
    }
}
