package com.xiliulou.electricity.entity.enterprise;


import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/**
 * 企业用户信息表(EnterpriseInfo)实体类
 *
 * @author Eclair
 * @since 2023-09-14 10:15:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_enterprise_info")
public class EnterpriseInfo {
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
     * 自主续费状态 0:不自主续费, 1:自主续费
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

    //企业状态 0:开启,1:关闭
    public static final Integer STATUS_OPEN = 0;
    public static final Integer STATUS_CLOSE = 1;

    //残值回收方式 0:以实际退电日期回收云豆,1:以实际未消耗天数回收云豆
    public static final Integer RECOVERY_MODE_RETURN = 0;
    public static final Integer RECOVERY_MODE_REALY = 1;

}
