package com.xiliulou.electricity.request.failureAlarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/11/7 14:22
 * @desc
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class WarnHandleRequest {
    @NotEmpty(message = "[告警数据]不能为空")
    private List<Long> warnIdList;
    
    /**
     * 是否同时处理相同告警 0-是 1-否
     */
    @NotNull(message = "[同时处理相同告警]不能为空")
    private Integer handleType;
    
    /**
     * 处理状态：1--处理中 2--已处理  3--忽略 4--转工单
     */
    @NotNull(message = "[处理方式]不能为空")
    private Integer handleStatus;
    
    /**
     * 备注
     */
    private String remark;
}
