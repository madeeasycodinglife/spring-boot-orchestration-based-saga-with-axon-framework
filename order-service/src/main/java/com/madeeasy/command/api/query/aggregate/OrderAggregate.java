package com.madeeasy.command.api.query.aggregate;

import com.madeeasy.command.api.query.command.CreateOrderCommand;
import com.madeeasy.command.api.query.event.OrderCreatedEvent;
import com.madeeasy.commands.CancelOrderCommand;
import com.madeeasy.commands.CompleteOrderCommand;
import com.madeeasy.events.OrderCancelledEvent;
import com.madeeasy.events.OrderCompletedEvent;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor
//@SuppressWarnings({"unused", "unchecked","rawtypes","serial"})
@SuppressWarnings("all")
@Aggregate
public class OrderAggregate {
    @AggregateIdentifier
    private String orderId;
    private String productId;
    private String userId;
    private String addressId;
    private Integer quantity;
    private String orderStatus;

    /**
     * aggregate not found in the event store :
     * <p>
     * The main reason for this exception is, When the axon is trying to save the aggregate it should create the aggragate first.
     * The CreateOrderCommand will create an Order, as it's name already suggests.
     * Hence, it should be handled by a constructor rather than a regular method. So, instead of this:
     *
     * @CommandHandler public *void* on(CreateOrderCommand command) {
     * apply(new OrderCreatedEvent(command.getOrderId()));
     * }
     * You should be doing this:
     * @CommandHandler public OrderAggregate(CreateOrderCommand command) {
     * apply(new OrderCreatedEvent(command.getOrderId()));
     * }
     * after using CreateOrderCommand in the Constructor , now you can use without Constructor note we are using
     * Constructor instead of a Regular Method as :: using an aggregate constructor with @CommandHandler for the
     * CreateOrderCommand provides a clean and standardized way to create a new instance of the OrderAggregate
     * <p>
     * For the successive events you should use same OrderId ,else also it will throw
     * <p>
     * handleThrowable(java.lang.Throwable,org.springframework.web.context.request.WebRequest)
     * org.axonframework.modelling.command.AggregateNotFoundException: The aggregate was not found in the event
     * store at org.axonframework.eventsourcing.EventSourcingRepository.doLoadWithLock(EventSourcingRepository.java:122)
     */

    /**
     * By using @CommandHandler on a constructor in an aggregate, you are defining a special type of command
     * handler called an "aggregate constructor." An aggregate constructor is responsible for creating
     * a new instance of the aggregate when a specific command is received.
     * <p>
     * In the case of the CreateOrderCommand, it makes sense to use an aggregate constructor as the command
     * handler because it represents the initial step in the lifecycle of an order. When the CreateOrderCommand
     * is dispatched, the aggregate constructor is invoked to create a new instance of the OrderAggregate.
     * This allows you to enforce any business rules, perform validations, and set the initial state of
     * the order within the aggregate.
     * /-------------------------------------------------------------------------------------------
     * When working with Axon Framework, it's common practice to use an aggregate constructor
     * for the initial creation of the aggregate and regular methods for handling subsequent commands.
     * /-------------------------------------------------------------------------------------------
     *
     * @param createOrderCommand
     */
    @CommandHandler
    public OrderAggregate(CreateOrderCommand createOrderCommand) {
        // validation
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
        BeanUtils.copyProperties(createOrderCommand, orderCreatedEvent);
        AggregateLifecycle.apply(orderCreatedEvent);
    }

    @EventSourcingHandler
    public void on(OrderCreatedEvent orderCreatedEvent) {
        this.orderId = orderCreatedEvent.getOrderId();
        this.productId = orderCreatedEvent.getProductId();
        this.userId = orderCreatedEvent.getUserId();
        this.addressId = orderCreatedEvent.getAddressId();
        this.quantity = orderCreatedEvent.getQuantity();
        this.orderStatus = orderCreatedEvent.getOrderStatus();
    }

    @CommandHandler
    public void on(CompleteOrderCommand completeOrderCommand) {
        // validate the command
        // publish the Order Completed event
        OrderCompletedEvent event = OrderCompletedEvent.builder()
                .orderId(completeOrderCommand.getOrderId())
                .orderStatus(completeOrderCommand.getOrderStatus())
                .build();
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(OrderCompletedEvent orderCompletedEvent) {
        this.orderId = orderCompletedEvent.getOrderId();
        this.orderStatus = orderCompletedEvent.getOrderStatus();
    }

    @CommandHandler
    public void on(CancelOrderCommand cancelOrderCommand) {
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .orderId(cancelOrderCommand.getOrderId())
                .orderStatus(cancelOrderCommand.getOrderStatus())
                .build();
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(OrderCancelledEvent orderCancelledEvent) {
        this.orderId = orderCancelledEvent.getOrderId();
        this.orderStatus = orderCancelledEvent.getOrderStatus();
    }
}























