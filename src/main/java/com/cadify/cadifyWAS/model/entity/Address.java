package com.cadify.cadifyWAS.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String addressKey;

    private String memberKey;

    private String address;
    private String addressDetail;
    private String addressLabel;


    @Builder
    public Address(String address, String addressDetail, String addressLabel, String memberKey) {
        this.memberKey = memberKey;
        this.address = address;
        this.addressDetail = addressDetail;
        this.addressLabel = addressLabel;
    }

    public void updateAddress(String address, String addressDetail, String addressLabel) {
        this.address = address;
        this.addressDetail = addressDetail;
        this.addressLabel = addressLabel;
    }
}
