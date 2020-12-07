package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.City;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.mapper.StoreMapper;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.service.CityService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.electricity.vo.StoreVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 门店表(TStore)表服务实现类
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@Service("tStoreService")
public class StoreServiceImpl implements StoreService {
    @Resource
    private StoreMapper storeMapper;
    @Autowired
    RedisService redisService;
    @Autowired
    CityService cityService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Store queryByIdFromDB(Integer id) {
        return this.storeMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Store queryByIdFromCache(Integer id){
        Store cacheStore=redisService.getWithHash(ElectricityCabinetConstant.CACHE_STORE +id,Store.class);
        if(Objects.nonNull(cacheStore)){
            return cacheStore;
        }
        Store store=storeMapper.queryById(id);
        if(Objects.isNull(store)){
            return null;
        }
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_STORE + id, store);
        return store;
    }
    
    /**
     * 新增数据
     *
     * @param store 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Store insert(Store store) {
        this.storeMapper.insert(store);
        return store;
    }

    /**
     * 修改数据
     *
     * @param store 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(Store store) {
       return this.storeMapper.update(store);
         
    }

    @Override
    public R save(Store store) {
        if(Objects.isNull(store.getUsableStatus())){
            store.setUsableStatus(Store.STORE_UN_USABLE_STATUS);
        }
        store.setCreateTime(System.currentTimeMillis());
        store.setUpdateTime(System.currentTimeMillis());
        store.setDelFlag(ElectricityCabinet.DEL_NORMAL);
        int insert= storeMapper.insertOne(store);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            //新增缓存
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_STORE + store.getId(), store);
            return null;
        });
        return R.ok();
    }

    @Override
    public R edit(Store store) {
        store.setUpdateTime(System.currentTimeMillis());
        int insert= storeMapper.insertOne(store);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            //更新缓存
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_STORE + store.getId(), store);
            return null;
        });
        return R.ok();
    }

    @Override
    public R delete(Integer id) {
        Store store=new Store();
        store.setId(id);
        store.setUpdateTime(System.currentTimeMillis());
        store.setDelFlag(ElectricityCabinet.DEL_DEL);
        int insert= storeMapper.insertOne(store);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            //删除缓存
            redisService.deleteKeys(ElectricityCabinetConstant.CACHE_STORE + id);
            return null;
        });
        return R.ok();
    }

    @Override
    public R queryList(StoreQuery storeQuery) {
        List<StoreVO> storeVOList= storeMapper.queryList(storeQuery);
        if(ObjectUtil.isNotEmpty(storeVOList)){
            storeVOList.parallelStream().forEach(e -> {
                //地区
                City city = cityService.queryByIdFromCache(e.getAreaId());
                if (Objects.nonNull(city)) {
                    e.setAreaName(city.getCity());
                    e.setPid(city.getPid());
                }
            });
        }
        return R.ok(storeVOList.stream().sorted(Comparator.comparing(StoreVO::getCreateTime).reversed()).collect(Collectors.toList()));
    }

    @Override
    public R disable(Integer id) {
        Store store=new Store();
        store.setId(id);
        store.setUpdateTime(System.currentTimeMillis());
        store.setUsableStatus(Store.STORE_UN_USABLE_STATUS);
        int insert= storeMapper.insertOne(store);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            //更新缓存
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_STORE + store.getId(), store);
            return null;
        });
        return R.ok();
    }

    @Override
    public R reboot(Integer id) {
        Store store=new Store();
        store.setId(id);
        store.setUpdateTime(System.currentTimeMillis());
        store.setUsableStatus(Store.STORE_USABLE_STATUS);
        int insert= storeMapper.insertOne(store);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            //更新缓存
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_STORE + store.getId(), store);
            return null;
        });
        return R.ok();
    }
}