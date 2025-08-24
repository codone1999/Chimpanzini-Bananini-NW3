package org.example.itbmshopbe.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Size(max = 100)
    @NotNull
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Size(max = 255)
    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @Size(max = 100)
    @NotNull
    @Column(name = "fullname", nullable = false, length = 100)
    private String fullname;

    @NotNull
    @Lob
    @Column(name = "role", nullable = false)
    private String role;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "createdOn")
    @CreationTimestamp
    private Instant createdOn;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updatedOn")
    @UpdateTimestamp
    private Instant updatedOn;
//
    //@NotNull
    //@ColumnDefault("'INACTIVE'")
    //@Lob
    //@Column(name = "status", nullable = false)
    //private String status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.INACTIVE;

    public enum Status {INACTIVE, ACTIVE}

}