package com.inn.cafe.POJO;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

@NamedQuery(name = "Bill.getAllBills",
            query = "select b from Bill b order by b.id desc")

@NamedQuery(name = "Bill.getBillByUserName" ,
            query="select b from Bill b where b.createdby=:username order by b.id desc")

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "bill")
public class Bill implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "contactnumber")
    private String contactnumber;

    @Column(name = "paymentmethod")
    private String paymentmethod;

    @Column(name = "total")
    private Integer total;

    @Column(name = "productdetail" , columnDefinition = "json")
    private String productdetail;

    @Column(name = "createdby")
    private String createdby;


    public Bill(String contactnumber, String email, String createdby, Integer id, String name, String paymentmethod, String productdetail, Integer total, String uuid) {
        this.contactnumber = contactnumber;
        this.email = email;
        this.createdby = createdby;
        this.id = id;
        this.name = name;
        this.paymentmethod = paymentmethod;
        this.productdetail = productdetail;
        this.total = total;
        this.uuid = uuid;
    }

    public Bill() {

    }
}
