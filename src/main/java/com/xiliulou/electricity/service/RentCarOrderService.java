package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.RentCarOrder;
import com.xiliulou.electricity.query.RentCarOrderQuery;

/**
 * 租车记录(TRentCarOrder)表服务接口
 *
 * @author makejava
 * @since 2020-12-08 15:09:08
 */
public interface RentCarOrderService {

    /**
     * 新增数据
     *
     * @param rentCarOrder 实例对象
     * @return 实例对象
     */
    RentCarOrder insert(RentCarOrder rentCarOrder);



    R queryList(RentCarOrderQuery rentCarOrderQuery);
}