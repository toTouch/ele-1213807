package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.mapper.ElectricityCabinetMapper;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.service.ElectricityCabinetModelService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 换电柜表(TElectricityCabinet)表服务实现类
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@Service("electricityCabinetService")
public class ElectricityCabinetServiceImpl implements ElectricityCabinetService {
    @Resource
    private ElectricityCabinetMapper electricityCabinetMapper;
    @Autowired
    private ElectricityCabinetModelService electricityCabinetModelService;
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCabinetBoxServiceImpl electricityCabinetBoxServiceImpl;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinet queryByIdFromDB(Long id) {
        return this.electricityCabinetMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinet queryByIdFromCache(Long id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<ElectricityCabinet> queryAllByLimit(int offset, int limit) {
        return this.electricityCabinetMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param electricityCabinet 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElectricityCabinet insert(ElectricityCabinet electricityCabinet) {
        this.electricityCabinetMapper.insert(electricityCabinet);
        return electricityCabinet;
    }

    /**
     * 修改数据
     *
     * @param electricityCabinet 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElectricityCabinet electricityCabinet) {
        return this.electricityCabinetMapper.update(electricityCabinet);

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.electricityCabinetMapper.deleteById(id) > 0;
    }

    @Override
    public R save(ElectricityCabinet electricityCabinet) {
        //TODO 判断参数
        electricityCabinet.setCreateTime(System.currentTimeMillis());
        electricityCabinet.setUpdateTime(System.currentTimeMillis());
        electricityCabinet.setDelFlag(ElectricityCabinet.DEL_NORMAL);
        //三元组
        List<ElectricityCabinet> existsElectricityCabinet = electricityCabinetMapper.selectList(new LambdaQueryWrapper<ElectricityCabinet>().eq(ElectricityCabinet::getProductKey, electricityCabinet.getProductKey()).eq(ElectricityCabinet::getDeviceName, electricityCabinet.getDeviceName()).eq(ElectricityCabinet::getDeviceSecret, electricityCabinet.getDeviceSecret()));
        if (DataUtil.collectionIsUsable(existsElectricityCabinet)) {
            return R.fail("换电柜的三元组已存在！");
        }
       /* //或换电柜编号
        List<ElectricityCabinet> existsElectricityCabinet = electricityCabinetMapper.selectList(new LambdaQueryWrapper<ElectricityCabinet>().eq(ElectricityCabinet::getSn, electricityCabinet.getSn()));
        if (DataUtil.collectionIsUsable(existsElectricityCabinet)) {
            return R.fail("换电柜编号已存在！");
        }*/
        //查找快递柜型号
        ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromDB(electricityCabinet.getModelId());
        if (Objects.isNull(electricityCabinetModel)) {
            return R.fail("未找到换电柜型号！");
        }
        electricityCabinetMapper.insertOne(electricityCabinet);

        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET +electricityCabinet.getId(), electricityCabinet);
        //添加快递柜格挡
        electricityCabinetBoxServiceImpl.batchInsertBoxByModelId(electricityCabinetModel, electricityCabinet.getId());
        return R.ok();
    }

    @Override
    public R edit(ElectricityCabinet electricityCabinet) {
        electricityCabinetMapper.update(electricityCabinet);
        return R.ok();
    }

    @Override
    public R delete(Long id) {
        return null;
    }

    @Override
    public R queryList(ElectricityCabinetQuery electricityCabinetQuery) {
        return null;
    }
}