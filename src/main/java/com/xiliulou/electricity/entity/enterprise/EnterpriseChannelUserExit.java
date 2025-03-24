package com.xiliulou.electricity.entity.enterprise;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 企业渠道邀请用户表(EnterpriseChannelUser)实体类
 *
 * @author Eclair
 * @since 2023-09-14 10:18:18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_enterprise_channel_user_exit")
public class EnterpriseChannelUserExit {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 企业添加用户的uid
     */
    private Long uid;
    
    /**
     * 企业用户表Id
     */
    private Long channelUserId;
    
    /**
     * 企业id
     */
    private Long enterpriseId;
    
    /**
     * 所属加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 租户ID
     */
    private Long tenantId;
    
    /**
     * 类型：0-待处理，1-成功，2-失败
     */
    private Integer type;
    
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
    
    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 操作人
     */
    private Long operateUid;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;
    
    public static final Integer TYPE_INIT = 0;
    public static final Integer TYPE_SUCCESS = 1;
    public static final Integer TYPE_FAIL = 2;
    
}
