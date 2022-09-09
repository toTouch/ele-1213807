package com.xiliulou.electricity.vo;

import lombok.Data;

@Data
public class PageDataAndCountVo<T> {
    T data;
    Long count;
}
