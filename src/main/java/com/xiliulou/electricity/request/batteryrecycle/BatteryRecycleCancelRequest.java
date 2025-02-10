package com.xiliulou.electricity.request.batteryrecycle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Set;

/**
 * @author maxiaodong
 * @date 2024/10/30 11:07
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatteryRecycleCancelRequest {
    /**
     * 电池snList
     */
    @NotEmpty(message = "电池sn不能为空")
    private Set<String> batterySnList;

    private Integer tenantId;

    private List<Long> franchiseeIdList;
}
