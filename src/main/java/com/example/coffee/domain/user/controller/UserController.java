package com.example.coffee.domain.user.controller;

import com.example.coffee.domain.BaseResponse;
import com.example.coffee.domain.user.dto.UserPointChargeReq;
import com.example.coffee.domain.user.facade.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserFacade userFacade;

    @PostMapping("/point")
    public ResponseEntity<BaseResponse> pointCharge(@RequestBody UserPointChargeReq userPointChargeReq) throws InterruptedException {
        BaseResponse baseResponse = BaseResponse.of(HttpStatus.OK, "menu");
        baseResponse.setData(userFacade.pointCharge(userPointChargeReq.getUserId(), userPointChargeReq.getPoint()));
        return ResponseEntity.ok(baseResponse);
    }

}
