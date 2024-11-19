/**
 *  Create date: 2024/8/27
 */

package com.xiliulou.electricity.entity.payparams;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/27 11:33
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WechatCertificateCacheEntity implements Serializable {
    
    private static final long serialVersionUID = 4075355867920600867L;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 微信证书
     */
    private List<String> certificates;
    
}
