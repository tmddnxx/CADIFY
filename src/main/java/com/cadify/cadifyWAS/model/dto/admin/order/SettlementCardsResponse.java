package com.cadify.cadifyWAS.model.dto.admin.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SettlementCardsResponse {
    private Long waitCount;
    private Long waitAmount;
    private Long completeCount;
    private Long completeAmount;
    private Long totalCount;
    private Long totalAmount;
}
