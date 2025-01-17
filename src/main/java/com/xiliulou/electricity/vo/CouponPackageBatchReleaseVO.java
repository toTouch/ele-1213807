package com.xiliulou.electricity.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @author : renhang
 * @description CouponPackageBatchReleaseVO
 * @date : 2025-01-17 14:30
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponPackageBatchReleaseVO {

    /**
     * 不存在手机号集合
     */
    private Set<String> notExistPhones;

    private String sessionId;

    /**
     * 前端是否需要轮训
     */
    private Integer isRequest;


    public static final Integer IS_REQUEST_YES = 1;

    public static final Integer IS_REQUEST_NO = 0;

}
