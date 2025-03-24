package com.xiliulou.electricity.vo.userinfo;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 运行小程序-会员列表-更多
 * @date 2025/2/24 11:38:15
 */
@Data
public class UserCarInfoProMoreVO {
    
    private Long uid;
    
    /**
     * 是否可解绑微信 0：不可解绑 1：可解绑
     */
    private Integer bindWX;
    
    /**
     * 是否可解绑支付宝 0：不可解绑 1：可解绑
     */
    private Integer bindAlipay;
    
    /**
     * 是否可退押 false：不可退 true：可退
     */
    private Boolean depositRefundFlag;
    
    /**
     * 是否可退租 false：不可退 true：可退
     */
    private Boolean carRentalPackageOrderRefundFlag;

}
