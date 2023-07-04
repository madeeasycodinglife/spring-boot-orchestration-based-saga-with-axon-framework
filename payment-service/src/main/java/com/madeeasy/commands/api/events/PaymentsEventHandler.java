package com.madeeasy.commands.api.events;

import com.madeeasy.commands.api.data.Payment;
import com.madeeasy.commands.api.data.PaymentRepository;
import com.madeeasy.events.PaymentCancelledEvent;
import com.madeeasy.events.PaymentProcessedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class PaymentsEventHandler {

    private final PaymentRepository paymentRepository;

    public PaymentsEventHandler(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @EventHandler
    public void on(PaymentProcessedEvent paymentProcessedEvent) {
        Payment payment = Payment.builder()
                .paymentId(paymentProcessedEvent.getPaymentId())
                .orderId(paymentProcessedEvent.getOrderId())
                .timestamp(new Date())
                .paymentStatus("COMPLETED")
                .build();
        paymentRepository.save(payment);
    }

    @EventHandler
    public void on(PaymentCancelledEvent paymentCancelledEvent) {
        Payment payment = paymentRepository.findById(paymentCancelledEvent.getPaymentId()).get();
        payment.setPaymentStatus(paymentCancelledEvent.getPaymentStatus());
        paymentRepository.save(payment);
    }
}
