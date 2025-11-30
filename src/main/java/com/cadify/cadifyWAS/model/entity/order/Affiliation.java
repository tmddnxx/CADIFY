package com.cadify.cadifyWAS.model.entity.order;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Affiliation {

    private String repName;
    private String repPhoneNumber;
    private String company;
    private String department;
}