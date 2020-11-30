package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.entity.City;
import lombok.Data;

import java.util.List;

/**
 * (Provincial)实体类
 *
 * @author makejava
 * @since 2020-11-25 16:20:43
 */
@Data
public class ProvincialVO {
    
    private Integer pid;
    
    private String provincial;

    private List<City> cityList;


}