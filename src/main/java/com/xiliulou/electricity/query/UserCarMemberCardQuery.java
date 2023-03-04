package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-03-02-19:10
 */
@Data
public class UserCarMemberCardQuery {

    /**
     * 用户Id
     */
    @NotNull(message = "用户id不能为空!")
    private Long uid;

    /**
     * 租赁方式
     */
    @NotNull(message = "租赁方式不能为空!")
    private String rentType;

    /**
     * 周期
     */
    @NotNull(message = "租赁周期不能为空!")
    private Integer validDays;


}
