package com.bahubba.bahubbabookclub.model.mapper;

import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.entity.BookClub;
import com.bahubba.bahubbabookclub.model.payload.NewBookClub;
import lombok.Generated;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookClubMapper {
    @Generated
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "disbanded", ignore = true)
    BookClub modelToEntity(NewBookClub newBookClub);

    @Generated
    BookClubDTO entityToDTO(BookClub bookClub);

    @Generated
    List<BookClubDTO> entityListToDTO(List<BookClub> bookClubs);
}
