package com.madeeasy.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CardDetails {
    private String name;
    private String cardNumber;
    private Integer validateUntilMonth;
    private Integer validateUntilYear;
    private Integer cvv;
}
