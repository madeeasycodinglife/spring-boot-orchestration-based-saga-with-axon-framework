package com.madeeasy.commands.api.data;

import com.madeeasy.model.CardDetails;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {
    @Id
    private String paymentId;
    private String orderId;
    private Date timestamp;
    private String paymentStatus;
}
