package com.madeeasy.commands.api.aggregate;

import com.madeeasy.commands.CancelPaymentCommand;
import com.madeeasy.commands.ValidatePaymentCommand;
import com.madeeasy.events.PaymentCancelledEvent;
import com.madeeasy.events.PaymentProcessedEvent;
import com.madeeasy.model.CardDetails;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

import java.util.UUID;

@Aggregate
@NoArgsConstructor
@SuppressWarnings("all")
@Slf4j
public class PaymentAggregate {
    @AggregateIdentifier
    private String paymentId;
    private String orderId;
    private CardDetails cardDetails;
    private String paymentStatus;

    @CommandHandler
    public PaymentAggregate(ValidatePaymentCommand validatePaymentCommand) {
        // validate payment details .
        // publish the payment processed event
        log.info("Executing ValidatePaymentCommand for orderId: {}" +
                        "and paymentId: {}", validatePaymentCommand.getOrderId(),
                validatePaymentCommand.getPaymentId());
        PaymentProcessedEvent paymentProcessedEvent = PaymentProcessedEvent.builder()
                .paymentId(UUID.randomUUID().toString())
                .orderId(validatePaymentCommand.getOrderId())
                .paymentStatus("COMPLETED")
                .build();
        AggregateLifecycle.apply(paymentProcessedEvent);
        log.info("PaymentProcessedEvent applied for orderId: {}" + validatePaymentCommand.getOrderId());
    }

    @EventSourcingHandler
    public void on(PaymentProcessedEvent paymentProcessedEvent) {
        this.paymentId = paymentProcessedEvent.getPaymentId();
        this.orderId = paymentProcessedEvent.getOrderId();
        this.cardDetails = paymentProcessedEvent.getCardDetails();
        this.paymentStatus = paymentProcessedEvent.getPaymentStatus();
    }

    @CommandHandler
    public void on(CancelPaymentCommand cancelPaymentCommand) {
        log.info("Executing CancelPaymentCommand for orderId: {}" +
                        "and paymentId: {}", cancelPaymentCommand.getOrderId(),
                cancelPaymentCommand.getPaymentId());
        PaymentCancelledEvent paymentCancelledEvent = new PaymentCancelledEvent();
        BeanUtils.copyProperties(cancelPaymentCommand, paymentCancelledEvent);
        AggregateLifecycle.apply(paymentCancelledEvent);
    }

    @EventSourcingHandler
    public void on(PaymentCancelledEvent paymentCancelledEvent) {
        this.paymentId = paymentCancelledEvent.getPaymentId();
        this.orderId = paymentCancelledEvent.getOrderId();
        this.paymentStatus = paymentCancelledEvent.getPaymentStatus();
    }
}
