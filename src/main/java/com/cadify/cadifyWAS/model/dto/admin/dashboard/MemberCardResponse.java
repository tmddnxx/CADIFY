package com.cadify.cadifyWAS.model.dto.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberCardResponse {
    @Builder.Default
    private Long totalValues = 0L;
    @Builder.Default
    private Long newValues = 0L;
    @Builder.Default
    private Long weeklyValues = 0L;
}
