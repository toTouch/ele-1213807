package com.xiliulou.electricity.dto;

import com.xiliulou.electricity.entity.UserInfo;
import lombok.Builder;
import lombok.Data;

/**
 * @ClassName: ExchangeAssertProcessDTO
 * @description:
 * @author: renhang
 * @create: 2024-11-12 14:53
 */
@Data
@Builder
public class ExchangeAssertProcessDTO {

    private UserInfo userInfo;

    /**
     * 柜机id
     */
    private Integer eid;

    /**
     * 仓门号
     */
    private Integer cellNo;

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 自主开仓的key
     */
    private String selfOpenCellKey;

    /**
     * 责任链内部存储，后续业务使用
     */
    private ExchangeChainDTO chainObject;
}
