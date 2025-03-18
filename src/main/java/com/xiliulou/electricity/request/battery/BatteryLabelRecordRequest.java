package com.xiliulou.electricity.request.battery;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@NoArgsConstructor
@AllArgsConstructor
public class BatteryLabelRecordRequest {
    
    @NotNull(message = "分页参数不能为空")
    private Long size;
    
    @NotNull(message = "分页参数不能为空")
    private Long offset;
    
    private Integer tenantId;
    
    @NotNull(message = "sn不能为空")
    private String sn;
    
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
    
    /**
     * 操作人uid
     */
    private Long operatorUid;
}
