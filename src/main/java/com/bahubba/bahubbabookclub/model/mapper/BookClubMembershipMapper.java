package com.bahubba.bahubbabookclub.model.mapper;

import com.bahubba.bahubbabookclub.model.dto.BookClubMembershipDTO;
import com.bahubba.bahubbabookclub.model.entity.BookClubMembership;
import lombok.Generated;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookClubMembershipMapper {
    @Generated
    @Mapping(target = "isCreator", source = "creator")
    BookClubMembershipDTO entityToDTO(BookClubMembership bookClubMembership);
}
