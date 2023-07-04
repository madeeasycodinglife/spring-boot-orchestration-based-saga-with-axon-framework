package com.madeeasy.command.api.query.event.handler;

import com.madeeasy.command.api.query.entity.Order;
import com.madeeasy.command.api.query.event.OrderCreatedEvent;
import com.madeeasy.command.api.query.repository.OrderRepository;
import com.madeeasy.events.OrderCancelledEvent;
import com.madeeasy.events.OrderCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@SuppressWarnings("all")
public class OrderEventsHandler {

    private final OrderRepository orderRepository;

    public OrderEventsHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @EventHandler
    public void on(OrderCreatedEvent orderCreatedEvent) {
        Order order = new Order();
        BeanUtils.copyProperties(orderCreatedEvent, order);
        orderRepository.save(order);
        log.info("Order created successfully for orderId: {} ", order.getOrderId());
    }

    @EventHandler
    public void on(OrderCompletedEvent orderCompletedEvent) {
        log.info("Order completed successfully for orderId: {} ", orderCompletedEvent.getOrderId());
        Order order = orderRepository.findById(orderCompletedEvent.getOrderId()).get();
        order.setOrderStatus(orderCompletedEvent.getOrderStatus());
        orderRepository.save(order);
    }

    @EventHandler
    public void on(OrderCancelledEvent orderCancelledEvent) {
        log.info("Order cancelled successfully for orderId: {} ", orderCancelledEvent.getOrderId());
        Order order = orderRepository.findById(orderCancelledEvent.getOrderId()).get();
        order.setOrderStatus(orderCancelledEvent.getOrderStatus());
        orderRepository.save(order);
    }
}
