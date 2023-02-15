package com.xiliulou.electricity.web.query;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author : eclair
 * @date : 2022/12/14 16:08
 */
@Data
public class CarControlRequest {
    /**
     * 0--解锁
     * 1--加锁
     */
    @Range(min = 0, max = 1, message = "锁状态不合法")
    @NotNull(message = "锁状态不能为空")
    private Integer lockType;
    
    @NotNull(message = "车辆Id不能为空")
    private Integer carId;
    
    public static final Integer TYPE_UN_LOCK = 0;
    
    public static final Integer TYPE_LOCK = 1;
    
}
