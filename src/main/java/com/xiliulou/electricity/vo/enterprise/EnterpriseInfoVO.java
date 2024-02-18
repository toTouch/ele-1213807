package com.xiliulou.electricity.vo.enterprise;

import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-14-10:30
 */
@Data
public class EnterpriseInfoVO {
    /**
     * 主键ID
     */
    private Long id;

    private String name;
    /**
     * 企业用户id
     */
    private Long uid;

    private String username;
    /**
     * 用户电话
     */
    private String phone;
    /**
     * 加盟商id
     */
    private Long franchiseeId;

    private String franchiseeName;
    
    private Long businessId;
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
     * 企业设置骑手续租状态总开关：0:不自主续费, 1:自主续费
     * @see RenewalStatusEnum
     */
    private Integer renewalStatus;
    
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

    private List<String> memcardName;
    
    private List<Long> packageIds;
    
    private Integer channelUserCount;
    
    /**
     * 会员代付权限 0：关，1：开
     */
    private Integer purchaseAuthority;
}
