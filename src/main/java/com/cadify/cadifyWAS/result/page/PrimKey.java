package com.cadify.cadifyWAS.result.page;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PrimKey {
    MEMBER_DEFAULT("memberKey");

    private final String key;
}
