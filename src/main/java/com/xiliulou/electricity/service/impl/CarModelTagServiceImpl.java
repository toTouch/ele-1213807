package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.CarModelTag;
import com.xiliulou.electricity.mapper.CarModelTagMapper;
import com.xiliulou.electricity.service.CarModelTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 车辆型号标签表(CarModelTag)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-14 15:55:53
 */
@Service("carModelTagService")
@Slf4j
public class CarModelTagServiceImpl implements CarModelTagService {

    @Resource
    private CarModelTagMapper carModelTagMapper;

    /**
     * 根据车辆型号ID集获取对应标签集
     *
     * @param carModelIdList 车辆型号ID集
     * @return 标签集
     */
    @Slave
    @Override
    public List<CarModelTag> selectByCarModelIds(List<Integer> carModelIdList) {
        if (CollectionUtils.isEmpty(carModelIdList)) {
            return null;
        }
        return carModelTagMapper.selectByCarModelIds(carModelIdList);
    }

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public CarModelTag selectByIdFromDB(Long id) {
        return this.carModelTagMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public CarModelTag selectByIdFromCache(Long id) {
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
    public List<CarModelTag> selectByPage(int offset, int limit) {
        return this.carModelTagMapper.selectByPage(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param carModelTag 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CarModelTag insert(CarModelTag carModelTag) {
        this.carModelTagMapper.insertOne(carModelTag);
        return carModelTag;
    }

    /**
     * 修改数据
     *
     * @param carModelTag 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(CarModelTag carModelTag) {
        return this.carModelTagMapper.update(carModelTag);

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
        return this.carModelTagMapper.deleteById(id) > 0;
    }

    @Override
    public Integer batchInsert(List<CarModelTag> carModelTagList) {
        if(CollectionUtils.isEmpty(carModelTagList)){
            return NumberConstant.ZERO;
        }
        return this.carModelTagMapper.batchInsert(carModelTagList);
    }

    @Override
    public Integer deleteByCarModelId(long carModelId) {
        return this.carModelTagMapper.deleteByCarModelId(carModelId);
    }

    @Override
    public List<CarModelTag> selectByCarModelId(Integer id) {
        return this.carModelTagMapper.selectList(new LambdaQueryWrapper<CarModelTag>().eq(CarModelTag::getCarModelId,id).orderByAsc(CarModelTag::getSeq));
    }
}
