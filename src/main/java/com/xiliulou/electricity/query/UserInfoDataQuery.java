package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserInfoDataQuery {
    /**
     * 起始
     */
    private Integer offset = 0;
    /**
     * 页大小
     */
    private Integer size = 10;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 电池
     */
    private String batterySn;
    /**
     * 查询类型
     * @see com.xiliulou.electricity.enums.UserInfoDataQueryEnum
     */
    @NotNull
    private Integer queryType;
    /**
     * 租户ID
     */
    private Integer tenantId;




}
