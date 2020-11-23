package kz.muminov.orderservice.service.kafka;

import kz.muminov.orderservice.model.entity.Meal;
import kz.muminov.orderservice.model.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderCreateServiceProducer {

    private static final String TOPIC = "restaurant-events";

    private final KafkaTemplate<String, Order> kafkaTemplate;

    public void mealQuantity(Order order){
        System.out.println("Producing event");
        this.kafkaTemplate.send(TOPIC, order);
    }

}
