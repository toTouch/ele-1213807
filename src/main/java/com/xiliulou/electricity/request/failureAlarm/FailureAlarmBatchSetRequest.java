package com.xiliulou.electricity.request.failureAlarm;


import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 故障预警设置 批量设置请求
 *
 * @author maxiaodong
 * @since 2023-12-15 14:06:24
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FailureAlarmBatchSetRequest {
    
    /**
     * 主键ID
     */
    @NotEmpty(message = "[主键ID]不能为空")
    private List<Long> idList;
    
    /**
     * 运营商可见(0-不可见， 1-可见)
     */
    @Range(min = 0, max = 1, message = "运营商可见不存在")
    @NotNull(message = "运营商可见不能为空")
    private Integer tenantVisible;
}
