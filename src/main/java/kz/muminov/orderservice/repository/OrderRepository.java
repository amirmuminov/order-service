package kz.muminov.orderservice.repository;

import kz.muminov.orderservice.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = "SELECT SUM(price) FROM order_meals om\n" +
            "JOIN orders o ON o.id = om.order_id\n" +
            "JOIN meal m ON m.id = om.meal_id\n" +
            "WHERE om.order_id = ?1", nativeQuery = true)
    double calculateBillByOrderId(Long id);

}
