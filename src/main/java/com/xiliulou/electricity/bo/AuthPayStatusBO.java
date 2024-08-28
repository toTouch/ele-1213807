package com.xiliulou.electricity.bo;

import lombok.Builder;
import lombok.Data;

/**
 * @ClassName: AuthPayStatusBO
 * @description: 代扣状态返回bo
 * @author: renhang
 * @create: 2024-08-28 15:01
 */
@Data
@Builder
public class AuthPayStatusBO {
    
    private String orderId;
    
    /**
     * 0:代扣成功；1:代扣处理中；2:代扣失败
     */
    private Integer orderStatus;
}
