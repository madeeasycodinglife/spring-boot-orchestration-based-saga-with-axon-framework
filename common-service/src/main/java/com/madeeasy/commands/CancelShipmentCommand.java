package com.madeeasy.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@AllArgsConstructor
public class CancelShipmentCommand {
    @TargetAggregateIdentifier
    private String shipmentId;
    private String orderId;
    private String paymentId;
    private final String shipmentStatus = "CANCELLED";
}
