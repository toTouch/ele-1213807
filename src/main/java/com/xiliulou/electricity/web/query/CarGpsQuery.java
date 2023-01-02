package com.xiliulou.electricity.web.query;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author : eclair
 * @date : 2023/1/2 09:21
 */
@Accessors(chain = true)
@Setter
@Getter
public class CarGpsQuery {
    
    private Integer carId;
    
    private String devId;
    
    private Long startTimeMills;
    
    private Long endTimeMills;
    
    private Integer size;
    
    private Integer offset;
    
    private String beginTime;
    
    private String endTime;
}
