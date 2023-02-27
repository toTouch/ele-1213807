package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author zgw
 * @date 2023/2/14 16:37
 * @mood
 */
@Data
public class UserInfoCarAddAndUpdate {
    
    @NotNull(message = "uId不能为空!")
    private Long uid;
    
    /**
     * 初始电池编号
     */
    @NotEmpty(message = "车辆编号不能为空!")
    private String carSn;
}
