package com.madeeasy.command.api.events;

import com.madeeasy.command.api.data.Shipment;
import com.madeeasy.command.api.data.ShipmentRepository;
import com.madeeasy.events.OrderShippedEvent;
import com.madeeasy.events.ShipmentCancelledEvent;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("all")
public class ShipmentEventHandler {

    private final ShipmentRepository shipmentRepository;

    public ShipmentEventHandler(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @EventHandler
    public void on(OrderShippedEvent orderShippedEvent) {
       /* Shipment shipment = Shipment.builder()
                .shipmentId(orderShippedEvent.getShipmentId())
                .orderId(orderShippedEvent.getOrderId())
                .shipmentStatus(orderShippedEvent.getShipmentStatus())
                .build();*/
        Shipment shipment = new Shipment();
        BeanUtils.copyProperties(orderShippedEvent, shipment);
        shipmentRepository.save(shipment);
    }

    @EventHandler
    public void on(ShipmentCancelledEvent shipmentCancelledEvent) {
        Shipment shipment = shipmentRepository.findById(shipmentCancelledEvent.getOrderId()).get();
        shipment.setShipmentStatus(shipmentCancelledEvent.getShipmentStatus());
        shipmentRepository.save(shipment);
    }
}
