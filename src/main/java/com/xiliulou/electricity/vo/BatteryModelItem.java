package com.xiliulou.electricity.vo;


import lombok.Builder;
import lombok.Data;

/**
 * @author : renhang
 * @description BatteryModelItem
 * @date : 2025-03-26 14:26
 **/
@Data
@Builder
public class BatteryModelItem {

    private String key;

    private String value;
}
