package com.xiliulou.electricity.dto.enterprise;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/11/12 15:20
 * @desc
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CloudBeanRefreshDTO {
    /**
     * 电池出仓门对应的换电订单
     */
    private Integer type;

    private Long enterpriseId;

    private String sessionId;
}
