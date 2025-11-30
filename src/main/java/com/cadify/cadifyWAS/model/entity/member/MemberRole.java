package com.cadify.cadifyWAS.model.entity.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MemberRole {
    VISITOR,
    ADMIN,
    USER,
    FACTORY,
    COMPANY;
}
