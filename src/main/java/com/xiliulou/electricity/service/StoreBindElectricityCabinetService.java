package com.xiliulou.electricity.service;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.StoreBindElectricityCabinet;

import java.util.List;

/**
 * (FranchiseeBind)表服务接口
 *
 * @author lxc
 * @since 2020-11-25 11:00:14
 */
public interface StoreBindElectricityCabinetService {

    void deleteByStoreId(Integer storeId);

    void insert(StoreBindElectricityCabinet storeBindElectricityCabinet);

    List<StoreBindElectricityCabinet> queryByStoreId(Integer id);
}