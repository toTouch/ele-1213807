package com.xiliulou.electricity.service;


import com.xiliulou.electricity.entity.StoreBind;

import java.util.List;

/**
 * (ElectricityCabinetBind)表服务接口
 *
 * @author lxc
 * @since 2020-11-25 11:00:14
 */
public interface StoreBindService {


    void insert(StoreBind storeBind);

    StoreBind queryByStoreId(Integer id);

    void deleteByStoreId(Integer id);
}