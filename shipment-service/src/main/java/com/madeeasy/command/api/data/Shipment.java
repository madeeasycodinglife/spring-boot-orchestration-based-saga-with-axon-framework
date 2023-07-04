package com.madeeasy.command.api.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Shipment {
    @Id
    private String shipmentId;
    private String orderId;
    private String paymentId;
    private String shipmentStatus;
}
