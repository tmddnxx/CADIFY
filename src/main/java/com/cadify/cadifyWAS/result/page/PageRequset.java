package com.cadify.cadifyWAS.result.page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PageRequset<T> {
    private PageRequestDto pageDto;
    private T dataDto;
}
