package com.cadify.cadifyWAS.mapper;

import com.cadify.cadifyWAS.model.dto.order.AddressDTO;
import com.cadify.cadifyWAS.model.entity.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public AddressDTO.Response addressToAddressDTO(Address address) {
        return AddressDTO.Response.builder()
                .addressKey(address.getAddressKey())
                .address(address.getAddress())
                .addressDetail(address.getAddressDetail())
                .addressLabel(address.getAddressLabel())
                .build();
    }
}
