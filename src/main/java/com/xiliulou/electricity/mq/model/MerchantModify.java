package com.xiliulou.electricity.mq.model;

import cn.hutool.core.util.IdUtil;
import lombok.Data;

/**
 * @ClassName: MerchantModify
 * @description:
 * @author: renhang
 * @create: 2024-03-26 10:37
 */
@Data
public class MerchantModify {
    private String msgId = IdUtil.simpleUUID();
    
    private Long merchantId;
    
    private Long uid;
}
