package com.example.coffee.domain.user.repository;

import com.example.coffee.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    User findByIdWithPessimisticLock(Long id);
}
