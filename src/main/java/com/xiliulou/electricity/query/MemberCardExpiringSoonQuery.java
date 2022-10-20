package com.xiliulou.electricity.query;

import lombok.Data;

/**
 * @author zgw
 * @date 2022/8/9 14:55
 * @mood
 */
@Data
public class MemberCardExpiringSoonQuery {
    private Long id;
    private String cardName;
    private Long memberCardExpireTime;
    private Long uid;
    private Integer tenantId;
}
