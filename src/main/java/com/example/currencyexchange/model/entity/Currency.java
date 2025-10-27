package com.example.currencyexchange.model.entity;

public class Currency {
    private long id;
    private String code;
    private String name;
    private String sign;

    public Currency() {}

    public Currency(String code, String name, String sign) {
        this.code = code;
        this.name = name;
        this.sign = sign;
    }

    public Currency(long id, String code, String name, String sign) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.sign = sign;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
