package com.bahubba.bahubbabookclub.model.mapper;

import com.bahubba.bahubbabookclub.model.dto.ReaderDTO;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.mapper.custom.EncodeMapping;
import com.bahubba.bahubbabookclub.model.mapper.custom.PasswordEncoderMapper;
import com.bahubba.bahubbabookclub.model.payload.NewReader;
import lombok.Generated;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = PasswordEncoderMapper.class)
public interface ReaderMapper {
    @Generated
    @Mapping(target = "id", ignore = true) // generated
    @Mapping(target = "memberships", ignore = true) // no memberships initially
    @Mapping(target = "role", ignore = true) // defaults to USER
    @Mapping(target = "joined", ignore = true) // defaults to now
    @Mapping(target = "departed", ignore = true) // default should be null
    @Mapping(source = "password", target = "password", qualifiedBy = EncodeMapping.class)
    Reader modelToEntity(NewReader newReader);

    @Generated
    ReaderDTO entityToDTO(Reader reader);

    @Generated
    List<ReaderDTO> entityListToDTO(List<Reader> readers);
}
