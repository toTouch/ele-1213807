package com.xiliulou.electricity.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : renhang
 * @description BatteryModelItem
 * @date : 2025-03-26 14:26
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatteryModelItem {

    private String key;

    private String value;
}
