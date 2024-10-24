/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/9/29
 */

package com.xiliulou.electricity.bo;

import lombok.Data;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/9/29 09:08
 */
@Data
public class FreeDepositUrlCacheBO {
    private String qrCode;
    
    private String path;
    
    private String extraData;
}
