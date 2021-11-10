package com.xiliulou.electricity.web.query;

import lombok.Data;

/**
 * @author : eclair
 * @date : 2021/8/4 4:06 下午
 */
@Data
public class ThirdCallBackUrlRequest {
    private String exchangeUrl;
    private String returnUrl;
    private String rentUrl;

}
