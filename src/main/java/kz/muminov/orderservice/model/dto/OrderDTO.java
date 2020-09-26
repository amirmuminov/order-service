package kz.muminov.orderservice.model.dto;

import kz.muminov.orderservice.model.entity.Employee;
import kz.muminov.orderservice.model.entity.Meal;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class OrderDTO {

    private Employee receiver;

    private List<Meal> meals;

}
