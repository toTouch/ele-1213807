package com.xiliulou.electricity.query;

import lombok.Data;

/**
 * @author zgw
 * @date 2022/10/11 9:39
 * @mood
 */
@Data public class BatteryMemberCardExpiringSoonQuery {
    private Long id;
    private String cardName;
    private Long memberCardExpireTime;
    private String memberCardExpireTimeStr;
    private Long uid;
    private Integer tenantId;
    private String thirdId;
    private String merchantMinProAppId;
    private String merchantMinProAppSecert;
    private String memberCardExpiringTemplate;
}
