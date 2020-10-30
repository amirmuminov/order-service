package kz.muminov.orderservice.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import kz.muminov.orderservice.exception.DefaultException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;
    private final ExceptionUtils exceptionUtils;

    @HystrixCommand(
            fallbackMethod = "fallbackCreateOrder",
            threadPoolProperties = {
                    @HystrixProperty(name = "coreSize", value = "100"),
                    @HystrixProperty(name = "maximumSize", value = "120"),
                    @HystrixProperty(name = "maxQueueSize", value = "50"),
                    @HystrixProperty(name = "allowMaximumSizeToDivergeFromCoreSize", value = "true")
            }
    )
    public Order createOrder(OrderDTO orderDTO){

        Order order = new Order();

        for (Meal meal: orderDTO.getMeals()){
            Meal existingMeal = restTemplate.getForObject("http://menu-service/meal/" + meal.getId(), Meal.class);
            order.getMeals().add(existingMeal);
        }

        Employee receiver = restTemplate.getForObject("http://employee-service/employee/" + orderDTO.getReceiver().getId(), Employee.class);

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

    public Order fallbackCreateOrder(OrderDTO orderDTO, Throwable e){
        Order order = new Order();
        order.setId(-1L);
        order.setStatus(OrderStatus.ERROR);
        log.error(e.getMessage());
        return order;
    }

}
