package com.xiliulou.electricity.query;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * @author zgw
 * @date 2023/4/10 10:37
 * @mood
 */
@Data
public class CarControlQuery {
    
    /**
     * 0--解锁 1--加锁
     */
    @Range(min = 0, max = 1, message = "锁状态不合法")
    @NotNull(message = "锁状态不能为空")
    private Integer lockType;
}
