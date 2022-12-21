package com.xiliulou.electricity.service;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.query.CallBackQuery;
import com.xiliulou.electricity.query.StoreAddAndUpdate;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.vo.MapVo;
import com.xiliulou.electricity.vo.StoreVO;
import org.apache.commons.lang3.tuple.Triple;

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

	Integer queryCountByFranchisee(Long uid);

	void updateById(Store store);

    List<MapVo> queryCountGroupByProvinceId(Integer tenantId);

    List<Long> queryStoreIdsByProvinceIdOrCityId(Integer tenantId,Integer pid,Integer cid);

    List<MapVo> queryCountGroupByCityId(Integer tenantId,Integer pid);

    List<Store> selectByFranchiseeId(Long id);

    Integer queryCountForHomePage(StoreQuery storeQuery);

    List<Long> queryStoreIdByFranchiseeId(List<Long> ids);

    List<Store> selectByFranchiseeIds(List<Long> franchiseeIds);

    Triple<Boolean, String, Object> selectListByQuery(StoreQuery storeQuery);
    
    List<Store> selectByStoreIds(List<Long> storeIds);

    Store queryFromCacheByProductAndDeviceName(String productKey, String deviceName);

    List<StoreVO> selectListByDistance(StoreQuery storeQuery);

    StoreVO selectDetailById(Long id);
}
