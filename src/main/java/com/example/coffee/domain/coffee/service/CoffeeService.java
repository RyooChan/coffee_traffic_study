package com.example.coffee.domain.coffee.service;

import com.example.coffee.domain.coffee.dto.CoffeeDataDto;
import com.example.coffee.domain.coffee.dto.CoffeeMenuRes;
import com.example.coffee.domain.coffee.entity.Coffee;
import com.example.coffee.domain.coffee.repository.CoffeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CoffeeService {

    private final ApiService<String> apiService;

    private final CoffeeRepository coffeeRepository;

    @Transactional(readOnly = true)
    @Cacheable("Coffee")
    public List<CoffeeMenuRes> coffeeMenuList() {
        List<CoffeeMenuRes> coffeeMenuRes = new ArrayList<>();
        List<Coffee> all = coffeeRepository.findAll();
        for (Coffee coffee : all) {
            coffeeMenuRes.add(new CoffeeMenuRes(coffee.getId(), coffee.getName(), coffee.getPrice()));
        }
        return coffeeMenuRes;
    }

    @Transactional(readOnly = true)
    @Cacheable("Coffee")
    public Coffee coffeeMenu(Long id) {
        return coffeeRepository.findById(id).orElseThrow();
    }

    public String callMockApiServer(CoffeeDataDto coffeeDataDto) {
        HttpHeaders httpHeaders = new HttpHeaders();
        String response = apiService
                .post("https://a4e9efea-0d64-4c0d-a9b3-671b3e5c4bce.mock.pstmn.io/mockapi/order", httpHeaders, coffeeDataDto, String.class)
                .getBody()
                ;
        return response;
    }

}
