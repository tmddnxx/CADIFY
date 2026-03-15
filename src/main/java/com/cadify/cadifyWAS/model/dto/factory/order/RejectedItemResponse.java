package com.cadify.cadifyWAS.model.dto.factory.order;

import com.cadify.cadifyWAS.model.entity.order.OrderReceivedStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RejectedItemResponse {
    private String status;
    private String rejectReasonCategory;
    private String rejectReasonDetail;

    @Builder
    public RejectedItemResponse(OrderReceivedStatus status, String rejectReasonCategory, String rejectReasonDetail){
        this.status = status.name();
        this.rejectReasonCategory = rejectReasonCategory;
        this.rejectReasonDetail = rejectReasonDetail;
    }
}
