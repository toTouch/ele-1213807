package com.xiliulou.electricity.vo.failureAlarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/1/8 11:38
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FailureWarnProportionVo {
    /**
     * 信号量Id
     */
    private String signalId;
    
    /**
     * 数量
     */
    private Integer count;
    
    /**
     * 值
     */
    private Integer value;
    
    /**
     * 名称
     */
    private String name;
    
    /**
     * 路径
     */
    private String path;
    
    private List<FailureWarnProportionVo> children;
}
