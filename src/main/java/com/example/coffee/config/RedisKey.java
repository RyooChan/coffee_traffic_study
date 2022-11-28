package com.example.coffee.config;

public enum RedisKey {

    HIT("hit:"),
    ;
    private final String name;

    RedisKey(String name){
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
