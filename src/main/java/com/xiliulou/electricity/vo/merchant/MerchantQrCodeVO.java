package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author maxiaodong
 * @date 2024/2/26 20:12
 * @desc
 */
@Data
public class MerchantQrCodeVO {
    private Long merchantId;
    private Long merchantUid;
    private Integer type;
    private String code;
}
