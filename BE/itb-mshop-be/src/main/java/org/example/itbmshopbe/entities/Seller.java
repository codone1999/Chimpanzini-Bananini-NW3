package org.example.itbmshopbe.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "seller")
public class Seller {
    @Id
    @Column(name = "account_id", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Size(max = 20)
    @Column(name = "mobile", length = 20)
    private String mobile;

    @Size(max = 50)
    @Column(name = "bank_account_no", length = 50)
    private String bankAccountNo;

    @Size(max = 100)
    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Size(max = 50)
    @Column(name = "national_card_no", length = 50)
    private String nationalCardNo;

    @Size(max = 255)
    @Column(name = "national_card_photo_front")
    private String nationalCardPhotoFront;

    @Size(max = 255)
    @Column(name = "national_card_photo_back")
    private String nationalCardPhotoBack;

}