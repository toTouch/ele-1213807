package com.xiliulou.electricity.service;

import com.xiliulou.electricity.query.CarProtocolQuery;
import com.xiliulou.electricity.vo.CarProtocolVO;
import org.apache.commons.lang3.tuple.Triple;

/**
 * @author: Kenneth
 * @Date: 2023/8/7 11:48
 * @Description:
 */
public interface CarProtocolService {
    CarProtocolVO findProtocolByQuery();
    
    Triple<Boolean, String, Object> update(CarProtocolQuery carProtocolQuery, Long uid);

}
