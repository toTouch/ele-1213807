package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-03-01-22:02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreeDepositCarMemberCardOrderQuery {
    /**
     * 租赁方式  WEEK_RENT：周租  ，MONTH_RENT：月租
     */
    @NotBlank(message = "租赁方式不能为空!")
    private String rentType;
    /**
     * 租赁周期
     */
    @NotNull(message = "租赁周期不能为空!")
    private Integer rentTime;
}
