package com.madeeasy.projection;

import com.madeeasy.model.CardDetails;
import com.madeeasy.model.User;
import com.madeeasy.queries.GetUserPaymentDetailsQuery;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class UserProjection {

    @QueryHandler
    public User getUserPaymentDetails(GetUserPaymentDetailsQuery query) {
        // Ideally Get details from the database.
        CardDetails cardDetails = CardDetails.builder()
                .name("Pabitra Bera")
                .validateUntilMonth(1)
                .validateUntilYear(2023)
                .cvv(123)
                .cardNumber("1234567890123456")
                .build();
        return User.builder()
                .userId(query.getUserId())
                .firstName("Pabitra")
                .lastName("Bera")
                .cardDetails(cardDetails)
                .build();
    }
}
