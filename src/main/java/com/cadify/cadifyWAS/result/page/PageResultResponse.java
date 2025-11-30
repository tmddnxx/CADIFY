package com.cadify.cadifyWAS.result.page;

import com.cadify.cadifyWAS.result.ResultCode;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

// 복합 데이터 반환 모델
@Getter
public class PageResultResponse<T> {
    private ResultCode resultCode;
    private List<T> data;
    private PageInfo pageInfo;

    public PageResultResponse(ResultCode resultCode, Page<T> page) {
        this.resultCode = resultCode;
        this.data = page.getContent();
        this.pageInfo = new PageInfo(
                (page.getNumber() + 1),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}