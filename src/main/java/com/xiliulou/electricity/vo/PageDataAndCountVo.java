package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor public class PageDataAndCountVo<T> {
    T data;
    Long count;
}
