package com.madeeasy.command.api.query.sage;

import com.madeeasy.command.api.query.event.OrderCreatedEvent;
import com.madeeasy.commands.*;
import com.madeeasy.events.*;
import com.madeeasy.model.User;
import com.madeeasy.queries.GetUserPaymentDetailsQuery;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Saga
@Slf4j
@SuppressWarnings("all")
public class OrderProcessingSaga {

    // Bear in mind that, where using "components" is on the saga, make then transient to not
    // have then serializable .
    @Autowired
    private transient CommandGateway commandGateway;
    @Autowired
    private transient QueryGateway queryGateway;

    /**
     * The value of "orderId" could be an identifier or a unique reference to an "order entity" within your system.
     * It could be a UUID, a database ID, or any other form of unique identifier that distinguishes one order from another.
     * <p>
     * In a real-world scenario where you have multiple events involved in a saga, it's common to use the same
     * association property throughout the entire saga [sart saga to end saga for particular chain of events connected
     * one after another] i.e. OrderCreatedEvent --> PaymentCompletedEvent --> ShipmentEvent, etc.
     * This ensures consistent correlation between the saga and the associated events.
     * <p>
     * Here are a few considerations to help you decide on the association property:
     * <p>
     * Common Identifier: If there is a common identifier that is present in all events related to a specific saga,
     * you can use that identifier as the association property. In your case, if "orderId" is present in all the events
     * (e.g., OrderCreatedEvent, PaymentCompletedEvent, ShipmentEvent, etc.), then you can use "orderId" as the association property.
     * <p>
     * Correlation Identifier: If there is no common identifier but there is a correlation between events, you can create
     * a new identifier that spans across all related events. This identifier can be generated when the saga starts and
     * then used as the association property in all subsequent events.
     * <p>
     * Multiple Associations: If a saga needs to correlate with different entities or processes, you might need multiple
     * association properties. For example, you may have a "orderId" for events related to orders, but you might also
     * have a "paymentId" for events related to payment processing.
     * <p>
     * Event Type: In some cases, you might want to differentiate based on the type of event. For example, if you have
     * different sagas handling different types of events related to an order, you might use the event type as an association property.
     */
    /**
     * Compensating Actions ::
     * =======================
     * In the context of event handling and rollback, the formula "Tn -> Cn-1" is known
     * as compensating actions. The idea is that if an event `Tn` (transaction `n`) has been processed and later
     * an error occurs or a rollback is needed, the system should trigger compensating actions `Cn-1`
     * (compensation for transaction `n-1`) to undo the effect of the previous transaction.
     * <p>
     * Here's a step-by-step description of how the formula works:
     * <p>
     * 1. Transaction `n` (`Tn`) is processed successfully, making changes to the system.
     * 2. At a later point, an error occurs or a rollback is needed, indicating that the effects of transaction `n` should be undone.
     * 3. The system identifies the compensating actions `Cn-1` that need to be executed to reverse the effects of transaction `n-1`.
     * 4. The compensating actions `Cn-1` are triggered, undoing the changes made by transaction `n`.
     * 5. After the compensating actions are completed, the system is in a consistent state as if transaction `n` had never occurred.
     */
    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    private void on(OrderCreatedEvent orderCreatedEvent) {
        log.info("OrderCreatedEvent in SagaEventHandler for orderId: {}", orderCreatedEvent.getOrderId());

        GetUserPaymentDetailsQuery getUserPaymentDetailsQuery = new GetUserPaymentDetailsQuery(orderCreatedEvent.getUserId());
        User user = null;
        try {
            user = queryGateway
                    .query(getUserPaymentDetailsQuery,
                            ResponseTypes.instanceOf(User.class)
                    ).join();
        } catch (Exception e) {
            log.error(e.getMessage());
            // start the compensating transaction .
            cancelOrderCommand(orderCreatedEvent.getOrderId());
        }

        try {
            ValidatePaymentCommand validatePaymentCommand = ValidatePaymentCommand.builder()
                    .paymentId(UUID.randomUUID().toString())
                    .orderId(orderCreatedEvent.getOrderId())
                    .cardDetails(user.getCardDetails())
                    .build();
            commandGateway.sendAndWait(validatePaymentCommand);
        } catch (Exception e) {
            log.error(e.getMessage());
            // start the compensating transaction .
            cancelOrderCommand(orderCreatedEvent.getOrderId());
        }
    }

    private void cancelOrderCommand(String orderId) {
        CancelOrderCommand cancelOrderCommand = new CancelOrderCommand(orderId);
        commandGateway.sendAndWait(cancelOrderCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(PaymentProcessedEvent paymentProcessedEvent) {
        log.info("ProcessedPaymentEvent in SagaEventHandler for orderId: {}", paymentProcessedEvent.getOrderId());
        try {
            ShipOrderCommand shipOrderCommand = ShipOrderCommand.builder()
                    .shipmentId(UUID.randomUUID().toString())
                    .orderId(paymentProcessedEvent.getOrderId())
                    .paymentId(paymentProcessedEvent.getPaymentId())
                    .build();
            commandGateway.sendAndWait(shipOrderCommand);
        } catch (Exception e) {
            log.error(e.getMessage());
            // start the compensating transaction .
            cancelPaymentCommand(paymentProcessedEvent);
        }
        log.info("ShipOrder command sent for orderId: {}", paymentProcessedEvent.getOrderId());
    }

    private void cancelPaymentCommand(PaymentProcessedEvent paymentProcessedEvent) {
        CancelPaymentCommand cancelPaymentCommand = new CancelPaymentCommand(
                paymentProcessedEvent.getPaymentId(),
                paymentProcessedEvent.getOrderId()
        );
        commandGateway.sendAndWait(cancelPaymentCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderShippedEvent orderShippedEvent) {
        log.info("OrderShippedEvent in SagaEventHandler for orderId: {}", orderShippedEvent.getOrderId());
        try {
            CompleteOrderCommand completeOrderCommand = CompleteOrderCommand.builder()
                    .orderId(orderShippedEvent.getOrderId())
                    .orderStatus("APPROVED")
                    .build();
            commandGateway.sendAndWait(completeOrderCommand);
        } catch (Exception e) {
            log.error(e.getMessage());
            // start the compensating transaction .
            cancelShipmentCommand(orderShippedEvent);
        }
    }

    private void cancelShipmentCommand(OrderShippedEvent orderShippedEvent) {
        CancelShipmentCommand cancelShipmentCommand = new CancelShipmentCommand(
                orderShippedEvent.getShipmentId(),
                orderShippedEvent.getOrderId(),
                orderShippedEvent.getPaymentId()
        );
        commandGateway.sendAndWait(cancelShipmentCommand);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderCompletedEvent orderCompletedEvent) {
        log.info("OrderCompletedEvent in SagaEventHandler for orderId: {}", orderCompletedEvent.getOrderId());
        // end of saga . if you want you may carry on for next command and event .
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderCancelledEvent orderCancelledEvent) {
        log.info("OrderCancelledEvent in SagaEventHandler for orderId: {}", orderCancelledEvent.getOrderId());
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(PaymentCancelledEvent paymentCancelledEvent) {
        log.info("PaymentCancelledEvent in SagaEventHandler for orderId: {}", paymentCancelledEvent.getPaymentId());
        cancelOrderCommand(paymentCancelledEvent.getOrderId());
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(ShipmentCancelledEvent shipmentCancelledEvent) {
        log.info("ShipmentCancelledEvent in SagaEventHandler for orderId: {}", shipmentCancelledEvent.getShipmentId());
        PaymentProcessedEvent paymentProcessedEvent =
                PaymentProcessedEvent.builder()
                        .orderId(shipmentCancelledEvent.getOrderId())
                        .paymentId(shipmentCancelledEvent.getPaymentId())
                        .build();
        cancelPaymentCommand(paymentProcessedEvent);
    }
}














