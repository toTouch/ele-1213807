package com.xiliulou.electricity.entity.warn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/6/21 9:49
 * @desc 故障告警消息中心短信回调
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarnNoteCallBack {
    /**
     * 租户id
     */
    private Integer tenantId;
    /**
     * 告警id
     */
    private String alarmId;
    /**
     * 消息类型
     */
    private String sessionId;
    
    /**
     * 短信次数
     */
    private Long count;
    
}
