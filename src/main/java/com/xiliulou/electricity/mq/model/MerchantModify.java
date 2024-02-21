package com.xiliulou.electricity.mq.model;

import cn.hutool.core.util.IdUtil;
import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-02-20-10:56
 */
@Data
public class MerchantModify {
    
    private String msgId = IdUtil.simpleUUID();
    
    private Long merchantId;
}
