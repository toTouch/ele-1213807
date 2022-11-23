package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-11-03-20:02
 */
@Data
@Builder
public class BoxOtherPropertiesQuery {
    
    @NotNull(message = "柜机id不能为空")
    private Long electricityCabinetId;
    @NotNull(message = "格挡号不能为空")
    private Integer cellNo;
}
