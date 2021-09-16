package com.xiliulou.electricity.service;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.query.StoreAddAndUpdate;
import com.xiliulou.electricity.query.StoreBindElectricityCabinetQuery;
import com.xiliulou.electricity.query.StoreQuery;

import java.util.HashMap;
import java.util.List;

/**
 * 门店表(TStore)表服务接口
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
public interface StoreService {

      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    Store queryByIdFromCache(Long id);

    R save(StoreAddAndUpdate storeAddAndUpdate);

    R edit(StoreAddAndUpdate storeAddAndUpdate);

    R delete(Long id);

    R queryList(StoreQuery storeQuery);

    R updateStatus(Long id,Integer usableStatus);

    Integer homeOne(List<Long> storeIdList,Integer tenantId);

    R showInfoByDistance(StoreQuery storeQuery);

	List<Store> queryByFranchiseeId(Long id);

	Store queryByUid(Long uid);

	R queryCount(StoreQuery storeQuery);

	R queryCountByFranchisee(StoreQuery storeQuery);

	List<HashMap<String, String>> homeThree(Long startTimeMilliDay, Long endTimeMilliDay, List<Long> storeIdList, Integer tenantId);

	void deleteByUid(Long uid);

	Integer queryCountByFranchiseeId(Long id);

	Integer queryByFanchisee(Long uid);

	void updateById(Store store);
}
