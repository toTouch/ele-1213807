/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/12
 */

package com.xiliulou.electricity.converter;

import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.request.payparams.ElectricityPayParamsRequest;
import com.xiliulou.electricity.vo.ElectricityPayParamsVO;
import org.springframework.beans.BeanUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    
    /**
     * 查询转换
     *
     * @param params
     * @author caobotao.cbt
     * @date 2024/6/12 18:14
     */
    public static List<ElectricityPayParamsVO> qryDoToVos(List<ElectricityPayParams> params) {
        return Optional.ofNullable(params).orElse(Collections.emptyList()).stream().map(p -> {
            ElectricityPayParamsVO electricityPayParamsVO = new ElectricityPayParamsVO();
            BeanUtils.copyProperties(p, electricityPayParamsVO);
            return electricityPayParamsVO;
        }).collect(Collectors.toList());
    }
}
