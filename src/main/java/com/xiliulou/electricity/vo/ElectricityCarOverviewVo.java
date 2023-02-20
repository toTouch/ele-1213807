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
    
    /**
     * 车辆型号
     */
    private String carModelName;
    
    /**
     * 门店
     */
    private String storeName;
    
    /**
     * 使用状态
     */
    private Integer lockType;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 手机号
     */
    private String phone;
    
}
