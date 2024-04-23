package com.xiliulou.electricity.vo.enterprise;

import com.xiliulou.electricity.entity.enterprise.EnterprisePackage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-10-09-16:39
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterpriseInfoPackageVO {
    /**
     * 主键ID
     */
    private Long id;
    
    private Long businessId;
    
    private String name;
    /**
     * 企业用户id
     */
    private Long uid;
    /**
     * 用户电话
     */
    private String phone;
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    /**
     * 企业状态 0:开启,1:关闭
     */
    private Integer status;
    /**
     * 残值回收方式 0:以实际退电日期回收云豆,1:以实际未消耗天数回收云豆
     */
    private Integer recoveryMode;
    /**
     * 企业云豆总数
     */
    private BigDecimal totalBeanAmount;
    /**
     * 租户ID
     */
    private Integer tenantId;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;
    /**
     * 备注
     */
    private String remark;
    
    private List<EnterprisePackage> enterprisePackage;
    
    /**
     * 会员代付权限 0：关，1：开
     */
    private Integer purchaseAuthority;
}
