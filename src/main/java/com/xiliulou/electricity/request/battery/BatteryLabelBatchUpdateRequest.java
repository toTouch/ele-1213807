package com.xiliulou.electricity.request.battery;

import com.xiliulou.electricity.enums.battery.BatteryLabelEnum;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author SJP
 * @date 2025-02-20 16:50
 **/
@Data
public class BatteryLabelBatchUpdateRequest {
    
    /**
     * 电池标签
     * @see BatteryLabelEnum
     */
    @NotNull(message = "电池标签不能为空")
    private Integer label;
    
    /**
     * 领用管理员id
     */
    private Long administratorId;
    
    /**
     * 领用商户id
     */
    private Long merchantId;
    
    /**
     * 电池sn集合
     */
    private List<String> snList;
}
