package kz.muminov.orderservice.service;

import kz.muminov.orderservice.model.dto.OrderBillDTO;
import kz.muminov.orderservice.model.dto.OrderDTO;
import kz.muminov.orderservice.model.entity.Employee;
import kz.muminov.orderservice.model.entity.Meal;
import kz.muminov.orderservice.model.entity.Order;
import kz.muminov.orderservice.model.enums.OrderStatus;
import kz.muminov.orderservice.repository.OrderRepository;
import kz.muminov.orderservice.util.ExceptionUtils;
import kz.muminov.orderservice.util.MessageCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;
    private final ExceptionUtils exceptionUtils;

    public Order createOrder(OrderDTO orderDTO){

        Order order = new Order();

        for (Meal meal: orderDTO.getMeals()){
            Meal existingMeal = restTemplate.getForObject("http://localhost:8081/meal/" + meal.getId(), Meal.class);
            order.getMeals().add(existingMeal);
        }

        Employee receiver = restTemplate.getForObject("http://localhost:8082/employee/" + orderDTO.getReceiver().getId(), Employee.class);

        order.setReceiver(receiver);

        return orderRepository.save(order);

    }

    public OrderBillDTO payBill(Long id){

        if (!orderRepository.existsById(id)){
            exceptionUtils.throwDefaultException(MessageCode.ORDER_DOES_NOT_EXIST);
        }

        Order order = orderRepository.findById(id).get();

        if(order.getStatus() != OrderStatus.NOT_PAYED){
            exceptionUtils.throwDefaultException(MessageCode.ORDER_STATUS_NOT_NOT_PAYED);
        }

        OrderBillDTO orderBillDTO = new OrderBillDTO();
        orderBillDTO.setOrder(order);
        orderBillDTO.setBill(orderRepository.calculateBillByOrderId(id));

        order.setStatus(OrderStatus.PAYED);
        order.setClosedDate(LocalDateTime.now());
        orderRepository.save(order);

        return orderBillDTO;

    }

    public Order cancelOrder(Long id){

        if (!orderRepository.existsById(id)){
            exceptionUtils.throwDefaultException(MessageCode.ORDER_DOES_NOT_EXIST);
        }

        Order order = orderRepository.findById(id).get();

        if(order.getStatus() == OrderStatus.PAYED || order.getStatus() == OrderStatus.CANCELED){
            exceptionUtils.throwDefaultException(MessageCode.ORDER_STATUS_IS_PAYED);
        }

        order.setClosedDate(LocalDateTime.now());
        order.setStatus(OrderStatus.CANCELED);

        return orderRepository.save(order);

    }

}
