package com.xiliulou.electricity.query.supper;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author: Ant
 * @Date 2024/4/22
 * @Description:
 **/
@Data
public class DelBatteryReq implements Serializable {
    
    private static final long serialVersionUID = -6548805638916127649L;
    
    private Integer tenantId;
    
    private List<String> batterySnList;
    
}
