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
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 电池序号列表
     */
    private List<String> batterySnList;
    
    /**
     * 暴力删除
     * <pre>
     *     0 - 非暴力
     *     1 - 暴力
     * </pre>
     */
    private Integer violentDel;
    
}
