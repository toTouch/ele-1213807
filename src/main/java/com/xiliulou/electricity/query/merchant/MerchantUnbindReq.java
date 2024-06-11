package com.xiliulou.electricity.query.merchant;


import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * <p>
 * Description: This class is MerchantUnbindReq!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/5/30
 **/
@Data
public class MerchantUnbindReq implements Serializable {
    
    private static final long serialVersionUID = 6764983040780816131L;
    
    /**
     * <p>
     *    Description: 解绑类型，此类型仅供操作记录使用,请勿删除
     *    <pre>
     *        0 -- 商户类型解绑
     *        1 -- 渠道员类型解绑
     *    </pre>
     * </p>
    */
    @NotNull(message = "解绑类型不能为空")
    private Integer type;
    
    @NotNull(message = "商户名称不能为空")
    private String merchantName;
    
    @NotNull(message = "商户id不能为空")
    private Long id;
    
    @NotNull(message = "商户openId不能为空")
    private String openId;
    
    @NotNull(message = "解绑原因不能为空")
    private String unbindReason;
}
