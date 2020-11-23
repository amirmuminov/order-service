package kz.muminov.orderservice.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import kz.muminov.orderservice.model.dto.OrderBillDTO;
import kz.muminov.orderservice.model.dto.OrderDTO;
import kz.muminov.orderservice.model.entity.Employee;
import kz.muminov.orderservice.model.entity.Meal;
import kz.muminov.orderservice.model.entity.Order;
import kz.muminov.orderservice.model.enums.OrderStatus;
import kz.muminov.orderservice.repository.OrderRepository;
import kz.muminov.orderservice.service.kafka.OrderCreateServiceProducer;
import kz.muminov.orderservice.util.ExceptionUtils;
import kz.muminov.orderservice.util.MessageCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
    private final OrderCreateServiceProducer orderCreateServiceProducer;

    @HystrixCommand(
            fallbackMethod = "fallbackCreateOrder",
            threadPoolKey = "createOrder",
            threadPoolProperties = {
                    @HystrixProperty(name = "coreSize", value = "100"),
                    @HystrixProperty(name = "maxQueueSize", value = "50")
            }
    )
    public Order createOrder(OrderDTO orderDTO){

        Order order = new Order();

        String credentials = "rest-client:passwordd";
        String encodedCredentials = new String(Base64.encodeBase64(credentials.getBytes()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + encodedCredentials);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        for (Meal meal: orderDTO.getMeals()){
            Meal existingMeal = restTemplate.exchange("http://menu-service/meal/" + meal.getId(),
                    HttpMethod.GET,
                    entity,
                    Meal.class).getBody();
            order.getMeals().add(existingMeal);
        }

        Employee receiver = getEmployee(orderDTO.getReceiver().getId());

        order.setReceiver(receiver);

        orderCreateServiceProducer.mealQuantity(order);

        return orderRepository.save(order);

    }

    @HystrixCommand(
            fallbackMethod = "getEmployeeFallback",
            threadPoolKey = "getEmployee",
            threadPoolProperties = {
                    @HystrixProperty(name = "coreSize", value = "100"),
                    @HystrixProperty(name = "maxQueueSize", value = "50")
            }
    )
    private Employee getEmployee(Long id){

        String apiCredentials = "rest-client:password";
        String encodedCredentials = new String(Base64.encodeBase64(apiCredentials.getBytes()));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Basic " + encodedCredentials);

        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

        Employee receiver = restTemplate.exchange("http://employee-service/employee/" + id,
                HttpMethod.GET,
                httpEntity,
                Employee.class).getBody();
        return receiver;
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

    public Employee getEmployeeFallback(Long id){
        Employee employee = new Employee();
        employee.setId(-1L);
        return employee;
    }

    public Order fallbackCreateOrder(OrderDTO orderDTO, Throwable e){
        Order order = new Order();
        order.setId(-1L);
        order.setStatus(OrderStatus.ERROR);
        log.error(e.getMessage());
        return order;
    }

}
