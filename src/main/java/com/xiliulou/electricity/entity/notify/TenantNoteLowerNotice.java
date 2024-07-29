package com.xiliulou.electricity.entity.notify;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/6/19 15:39
 * @desc
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantNoteLowerNotice {
    /**
     * 平台名称
     */
    private String platformName;
    
    /**
     * 短信次数
     */
    private Long noteNum;
}
