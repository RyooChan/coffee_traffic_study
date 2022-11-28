package com.example.coffee.domain.user.entity;

import com.example.coffee.domain.BaseEntity;
import com.example.coffee.domain.exception.BusinessException;
import com.example.coffee.domain.exception.PointException;
import lombok.*;

import javax.persistence.Entity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    private Integer point;

    public User(Long id, Integer point){
        this.id = id;
        this.point = point;
    }
    public void pointCharge(User user, Integer point){
        user.point = user.point + point;
    }

    public void pointUse(User user, Integer point) {
        if(user.point - point < 0){
            throw new PointException();
        }
        user.point = user.point - point;
    }
}
