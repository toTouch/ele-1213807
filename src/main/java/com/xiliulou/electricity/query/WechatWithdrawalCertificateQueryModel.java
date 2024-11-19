/**
 *  Create date: 2024/6/13
 */

package com.xiliulou.electricity.query;

import lombok.Data;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/6/13 14:31
 */
@Data
public class WechatWithdrawalCertificateQueryModel {
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
}
