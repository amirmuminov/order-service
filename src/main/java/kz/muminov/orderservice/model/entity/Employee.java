package kz.muminov.orderservice.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import kz.muminov.orderservice.model.enums.EmployeeStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotEmpty
    private String firstName;

    @NotNull
    @NotEmpty
    private String lastName;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime hireDate = LocalDateTime.now();

    private LocalDateTime fireDate;

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate birthDate;

    private double salary;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

}
