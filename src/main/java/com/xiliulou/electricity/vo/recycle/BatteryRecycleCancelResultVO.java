package com.xiliulou.electricity.vo.recycle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * @author maxiaodong
 * @date 2024/10/30 14:51
 * @desc 电池回收取消结果VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatteryRecycleCancelResultVO {
    
    /**
     * 录入条数
     */
    private Integer successCount;
    
    /**
     * 录入失败条数
     */
    private Integer failCount;
    
    /**
     * 录入失败电池sn列表
     */
    private Set<String> failBatterySnList;
}
