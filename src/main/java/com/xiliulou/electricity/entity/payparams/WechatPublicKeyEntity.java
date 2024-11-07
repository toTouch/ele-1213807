package com.xiliulou.electricity.entity.payparams;


import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * Description: This class is WechatPublicKeyEntity!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/11/1
 **/
@Data
public class WechatPublicKeyEntity implements Serializable {
    
    private static final long serialVersionUID = -3719943710307609918L;
    
    // 主键
    private Long id;
    
    // 租户id
    private Integer tenantId;
    
    // 公钥ID
    private String pubKeyId;
    
    // 公钥内容
    private String pubKey;
    
    // 上传时间
    private Long uploadTime;
    
    // 配置参数表主键id
    private Long payParamsId;
    
    // 加盟商id
    private Long franchiseeId;
    
    // 删除标识：0-未删除 1-已删除
    private Integer delFlag;
    
    // 创建时间
    private Long createTime;
    
    // 更新时间
    private Long updateTime;
    
}
