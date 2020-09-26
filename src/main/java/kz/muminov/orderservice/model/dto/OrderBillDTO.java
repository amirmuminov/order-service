package kz.muminov.orderservice.model.dto;

import kz.muminov.orderservice.model.entity.Order;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderBillDTO {

    private Order order;

    private double bill;

}
