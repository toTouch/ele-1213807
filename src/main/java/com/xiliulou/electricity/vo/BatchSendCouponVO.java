package com.xiliulou.electricity.vo;

import lombok.Data;

import java.util.Set;

/**
 * @author : eclair
 * @date : 2024/2/18 19:19
 */
@Data
public class BatchSendCouponVO {
    private Set<String> notExistPhones;
    private String sessionId;
}
