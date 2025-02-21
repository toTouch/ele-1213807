package com.xiliulou.electricity.request.battery;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author SJP
 * @date 2025-02-21 14:36
 **/
@Data
@Builder
public class BatteryLabelRecordRequest {
    
    private Long size;
    
    private Long offset;
    
    private Integer tenantId;
    
    private Long sn;
    
    private Long franchiseeId;
    
    /**
     * 查询范围-开始时间
     */
    private Long startTime;
    
    /**
     * 查询范围-结束时间
     */
    private Long endTime;
    
    private List<Long> franchiseeIds;
    
    /**
     * 查询范围-开始时间
     */
    private String startTimeStr;
    
    /**
     * 查询范围-结束时间
     */
    private String endTimeStr;
}
