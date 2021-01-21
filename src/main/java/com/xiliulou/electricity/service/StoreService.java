package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.query.StoreAddAndUpdate;
import com.xiliulou.electricity.query.StoreQuery;

/**
 * 门店表(TStore)表服务接口
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
public interface StoreService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    Store queryByIdFromDB(Integer id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    Store queryByIdFromCache(Integer id);

    R save(StoreAddAndUpdate storeAddAndUpdate);

    R edit(StoreAddAndUpdate storeAddAndUpdate);

    R delete(Integer id);

    R queryList(StoreQuery storeQuery);

    R disable(Integer id);

    R reboot(Integer id);

    Integer homeTwoTotal();

    Integer homeTwoBusiness();

    R rentBattery(StoreQuery storeQuery);

    R rentCar(StoreQuery storeQuery);
}