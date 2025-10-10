package org.example.itbmshopbe.entities;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "customer_id", nullable = false)
    private Account customer;

    @ColumnDefault("'PENDING'")
    @Lob
    @Column(name = "status")
    private String status;

    @Size(max = 255)
    @Column(name = "orderNote")
    private String orderNote;

    @Size(max = 255)
    @NotNull
    @Column(name = "shippingAddress", nullable = false)
    private String shippingAddress;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "createdOn")
    private Instant createdOn;

    @CreationTimestamp
    @Column(name = "PaymentDate")
    private Instant paymentDate;

}