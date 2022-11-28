package com.example.coffee.domain.coffee.entity;

import com.example.coffee.domain.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
//@Cacheable
public class Coffee extends BaseEntity {
    private String name;
    private Integer price;

    public Coffee(Long id, String name, Integer price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
}
