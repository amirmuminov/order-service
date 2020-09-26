package kz.muminov.orderservice.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import kz.muminov.orderservice.model.enums.OrderStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate = LocalDateTime.now();

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closedDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.NOT_PAYED;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private Employee receiver;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "order_meals",
            joinColumns = {@JoinColumn(name = "order_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "meal_id", referencedColumnName = "id")}
    )
    private List<Meal> meals = new ArrayList<>();


}
