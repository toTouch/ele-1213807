package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetPower;
import com.xiliulou.electricity.mapper.ElectricityCabinetPowerMapper;
import com.xiliulou.electricity.query.ElectricityCabinetPowerQuery;
import com.xiliulou.electricity.service.ElectricityCabinetPowerService;
import com.xiliulou.electricity.utils.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;

/**
 * 换电柜电量表(ElectricityCabinetPower)表服务实现类
 *
 * @author makejava
 * @since 2021-01-27 16:22:44
 */
@Service("electricityCabinetPowerService")
@Slf4j
public class ElectricityCabinetPowerServiceImpl implements ElectricityCabinetPowerService {
    @Resource
    private ElectricityCabinetPowerMapper electricityCabinetPowerMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetPower queryByIdFromDB(Long id) {
        return this.electricityCabinetPowerMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public  ElectricityCabinetPower queryByIdFromCache(Long id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<ElectricityCabinetPower> queryAllByLimit(int offset, int limit) {
        return this.electricityCabinetPowerMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param electricityCabinetPower 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElectricityCabinetPower insert(ElectricityCabinetPower electricityCabinetPower) {
        this.electricityCabinetPowerMapper.insert(electricityCabinetPower);
        return electricityCabinetPower;
    }

    /**
     * 修改数据
     *
     * @param electricityCabinetPower 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElectricityCabinetPower electricityCabinetPower) {
       return this.electricityCabinetPowerMapper.update(electricityCabinetPower);
         
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
        return this.electricityCabinetPowerMapper.deleteById(id) > 0;
    }

    @Override
    public Integer insertOrUpdate(ElectricityCabinetPower electricityCabinetPower) {
        return this.electricityCabinetPowerMapper.insertOrUpdate(electricityCabinetPower);
    }

    @Override
    public R queryList(ElectricityCabinetPowerQuery electricityCabinetPowerQuery) {
        Page page = PageUtil.getPage(electricityCabinetPowerQuery.getOffset(), electricityCabinetPowerQuery.getSize());
        electricityCabinetPowerMapper.queryList(page,electricityCabinetPowerQuery);
        return R.ok(page);
    }

}