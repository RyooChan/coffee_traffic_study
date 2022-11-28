package com.example.coffee.domain.user.service;

import com.example.coffee.domain.exception.BusinessException;
import com.example.coffee.domain.user.dto.UserPointChargeRes;
import com.example.coffee.domain.user.entity.User;
import com.example.coffee.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserPointChargeRes pointCharge(Long id, Integer point){
        User user = userRepository.findByIdWithPessimisticLock(id);
        user.pointCharge(user, point);
        return new UserPointChargeRes(user.getId(), user.getPoint());
    }

    public void pointUse(Long id, Integer point) {
        User user = userRepository.findByIdWithPessimisticLock(id);
        try {
            user.pointUse(user, point);
        } catch(BusinessException e){
            throw new BusinessException(e.getErrorCode());
        }
    }

}
