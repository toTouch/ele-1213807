package com.xiliulou.electricity.vo.recycle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/10/30 14:51
 * @desc
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatteryRecycleSaveResultVO {
    
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
    private List<String> notExistBatterySnList;
}
