package com.bahubba.bahubbabookclub.model.mapper;

import com.bahubba.bahubbabookclub.model.dto.MembershipRequestDTO;
import com.bahubba.bahubbabookclub.model.entity.MembershipRequest;
import lombok.Generated;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Mapping logic for Membership Requests
 */
@Mapper(componentModel = "spring")
public interface MembershipRequestMapper {
    @Generated
    MembershipRequestDTO entityToDTO(MembershipRequest membershipRequest);

    @Generated
    List<MembershipRequestDTO> entityListToDTO(List<MembershipRequest> membershipRequests);

    @Generated
    Page<MembershipRequestDTO> entityPageToDTOPage(Page<MembershipRequest> membershipRequests);
}
