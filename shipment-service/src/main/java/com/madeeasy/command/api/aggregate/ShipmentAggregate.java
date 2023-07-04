package com.madeeasy.command.api.aggregate;

import com.madeeasy.commands.CancelShipmentCommand;
import com.madeeasy.commands.ShipOrderCommand;
import com.madeeasy.events.OrderShippedEvent;
import com.madeeasy.events.ShipmentCancelledEvent;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

@Aggregate
@NoArgsConstructor
@SuppressWarnings("all")
public class ShipmentAggregate {
    @AggregateIdentifier
    private String shipmentId;
    private String orderId;
    private String paymentId;
    private String shipmentStatus;

    @CommandHandler
    public ShipmentAggregate(ShipOrderCommand shipOrderCommand) {
        // validate the command
        // publish the order shipped event
        OrderShippedEvent orderShippedEvent = OrderShippedEvent.builder()
                .shipmentId(shipOrderCommand.getShipmentId())
                .orderId(shipOrderCommand.getOrderId())
                .paymentId(shipOrderCommand.getPaymentId())
                .shipmentStatus("COMPLETED")
                .build();
        AggregateLifecycle.apply(orderShippedEvent);
    }

    @EventSourcingHandler
    public void on(OrderShippedEvent orderShippedEvent) {
        this.shipmentId = orderShippedEvent.getShipmentId();
        this.orderId = orderShippedEvent.getOrderId();
        this.paymentId = orderShippedEvent.getPaymentId();
        this.shipmentStatus = orderShippedEvent.getShipmentStatus();
    }

    @CommandHandler
    public void on(CancelShipmentCommand cancelShipmentCommand) {
        ShipmentCancelledEvent event = new ShipmentCancelledEvent();
        BeanUtils.copyProperties(cancelShipmentCommand, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(ShipmentCancelledEvent event) {
        this.shipmentId = event.getShipmentId();
        this.orderId = event.getOrderId();
        this.shipmentStatus = event.getShipmentStatus();
    }
}



















