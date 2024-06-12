/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/12
 */

package com.xiliulou.electricity.converter;

import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.request.payparams.ElectricityPayParamsRequest;
import org.springframework.beans.BeanUtils;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/6/12 16:51
 */
public class ElectricityPayParamsConverter {
    
    /**
     * 操作参数转换
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/6/12 16:55
     */
    public static ElectricityPayParams optRequestToDO(ElectricityPayParamsRequest request) {
        ElectricityPayParams payParams = new ElectricityPayParams();
        BeanUtils.copyProperties(request, payParams);
        return payParams;
    }
    
}
