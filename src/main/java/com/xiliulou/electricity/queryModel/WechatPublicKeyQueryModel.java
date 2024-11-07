package com.xiliulou.electricity.queryModel;


import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * Description: This class is WechatPublicKeyQueryModel!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/11/1
 **/
@Data
@Builder
public class WechatPublicKeyQueryModel {
    
    // 主键
    public Long id;
    
    // 租户id
    public Integer tenantId;
    
    // 公钥ID
    public String pubKeyId;
    
    // 公钥内容
    public String pubKey;
    
    // 上传时间
    public Long uploadTime;
    
    // 配置参数表主键id
    public Long payParamsId;
    
    // 加盟商id
    public Long franchiseeId;
    
    // 加盟商ids
    public List<Long> franchiseeIds;
    
}
