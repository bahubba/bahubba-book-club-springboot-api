package com.bahubba.bahubbabookclub.model.payload;

import com.bahubba.bahubbabookclub.model.enums.Publicity;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Getter
@Component
public class NewBookClub {
    private String name;
    private String imageURL;
    private String description;
    private Publicity publicity;
}
