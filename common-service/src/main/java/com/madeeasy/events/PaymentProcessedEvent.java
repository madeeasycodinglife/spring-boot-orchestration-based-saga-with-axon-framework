package com.madeeasy.events;

import com.madeeasy.model.CardDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentProcessedEvent {

    private String paymentId;
    private String orderId;
    private CardDetails cardDetails;
    private String paymentStatus;
}
