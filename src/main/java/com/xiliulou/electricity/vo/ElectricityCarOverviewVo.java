package com.xiliulou.electricity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author zgw
 * @date 2023/2/17 14:27
 * @mood
 */
@Data
public class ElectricityCarOverviewVo {
    
    private Integer id;
    
    /**
     * 车辆sn
     */
    private String sn;
    
    /**
     * 地址经度
     */
    private Double longitude;
    
    /**
     * 地址纬度
     */
    private Double latitude;
}
