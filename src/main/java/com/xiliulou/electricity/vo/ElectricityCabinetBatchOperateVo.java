package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zgw
 * @date 2023/3/30 18:51
 * @mood
 */
@Data
public class ElectricityCabinetBatchOperateVo {
    
    private Integer id;
    
    private String name;
    
    private String sn;
    
    private String productKey;
    
    private String deviceName;
    
    private Integer onlineStatus;

    private String modelName;
}
