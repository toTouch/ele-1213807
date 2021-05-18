package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-11 18:23
 **/
@Data
@Builder
public class MemberCardOrderQuery {

    private String phone;
    private String orderId;
    private Integer cardType;
    private Long queryStartTime;
    private Long queryEndTime;
}
