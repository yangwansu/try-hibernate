package org.slipp.masil.hibernate;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Entity
@Table(name = "PRODUCT")
public class Product {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    public Product() {
    }

    public Product(String name) {
        this.name = name;
    }
}
