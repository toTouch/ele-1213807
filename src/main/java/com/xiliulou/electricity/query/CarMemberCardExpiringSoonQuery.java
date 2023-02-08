package com.xiliulou.electricity.query;

import lombok.Data;

/**
 * @author zgw
 * @date 2022/10/11 11:17
 * @mood
 */
@Data public class CarMemberCardExpiringSoonQuery {
    private Long id;
    private String cardName;
    private Long rentCarMemberCardExpireTime;
    private String rentCarMemberCardExpireTimeStr;
    private Long uid;
    private Integer tenantId;
    private String thirdId;
    private String merchantMinProAppId;
    private String merchantMinProAppSecert;
    private String memberCardExpiringTemplate;
    private String rentCarOrderId;
}
