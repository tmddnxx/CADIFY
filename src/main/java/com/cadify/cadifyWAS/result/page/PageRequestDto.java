package com.cadify.cadifyWAS.result.page;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@AllArgsConstructor
@NoArgsConstructor
public class PageRequestDto {
    @Positive
    private int page = 1;
    @Positive
    private int size = 10;
    private int sort = 0;

    public PageRequest of(PrimKey key){
        if(sort == 0){
            return PageRequest.of(page, size, Sort.by(key.getKey()).ascending());
        }
        else{
            return PageRequest.of(page, size, Sort.by(key.getKey()).descending());
        }
    }
}
