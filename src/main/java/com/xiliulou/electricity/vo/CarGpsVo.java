package com.xiliulou.electricity.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author : eclair
 * @date : 2023/1/2 11:03
 */
@Data
@Accessors(chain = true)
public class CarGpsVo {
    private String devId;
    private Double longitude;
    private Double latitude;
    private Long createTime;
}
