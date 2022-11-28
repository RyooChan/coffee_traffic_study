package com.example.coffee.domain.user.facade;

import com.example.coffee.domain.exception.BusinessException;
import com.example.coffee.domain.exception.ExceptionCode;
import com.example.coffee.domain.user.dto.UserPointChargeRes;
import com.example.coffee.domain.user.entity.User;
import com.example.coffee.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFacade {
    private final UserService userService;

    public UserPointChargeRes pointCharge(Long id, Integer point) throws InterruptedException {
        while(true){
            try{
                return userService.pointCharge(id, point);
            } catch(BusinessException e){
                Thread.sleep(50);
            }
        }
    }

    public void pointUse(Long id, Integer point) throws InterruptedException {
        while(true) {
            try{
                userService.pointUse(id, point);
                return;
            } catch (BusinessException e){
                if(e.getErrorCode().equals(ExceptionCode.POINT_USE_MINUS)){
                    Thread.currentThread().interrupt();
                    throw new BusinessException(e.getErrorCode());
                }
                Thread.sleep(50);
            }
        }
    }
}
