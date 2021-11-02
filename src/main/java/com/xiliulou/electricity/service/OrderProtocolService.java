package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.OrderProtocolQuery;
import org.apache.commons.lang3.tuple.Triple;

/**
 * (Faq)表服务接口
 *
 * @author makejava
 * @since 2021-09-26 14:06:23
 */
public interface OrderProtocolService {

    R queryOrderProtocol();


    Triple<Boolean, String, Object> update(OrderProtocolQuery orderProtocolQuery);

}
