package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetServer;
import com.xiliulou.electricity.entity.ElectricityCabinetServerOperRecord;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.mapper.ElectricityCabinetServerOperRecordMapper;
import com.xiliulou.electricity.service.ElectricityCabinetServerOperRecordService;
import com.xiliulou.electricity.service.ElectricityCabinetServerService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.vo.ElectricityCabinetServerOperRecordVo;
import com.xiliulou.electricity.vo.ElectricityCabinetServerVo;
import com.xiliulou.electricity.vo.PageDataAndCountVo;
import java.util.ArrayList;
import java.util.Objects;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * (ElectricityCabinetServerOperRecord)表服务实现类
 *
 * @author Hardy
 * @since 2022-09-26 17:54:54
 */
@Service("electricityCabinetServerOperRecordService") @Slf4j public class ElectricityCabinetServerOperRecordServiceImpl
    implements ElectricityCabinetServerOperRecordService {
    @Resource private ElectricityCabinetServerOperRecordMapper electricityCabinetServerOperRecordMapper;
    @Autowired private ElectricityCabinetService electricityCabinetService;
    @Autowired private ElectricityCabinetServerService electricityCabinetServerService;
    @Autowired private TenantService tenantService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override public ElectricityCabinetServerOperRecord queryByIdFromDB(Long id) {
        return this.electricityCabinetServerOperRecordMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override public ElectricityCabinetServerOperRecord queryByIdFromCache(Long id) {
        return null;
    }

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override public List<ElectricityCabinetServerOperRecord> queryAllByLimit(int offset, int limit) {
        return this.electricityCabinetServerOperRecordMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param electricityCabinetServerOperRecord 实例对象
     * @return 实例对象
     */
    @Override @Transactional(rollbackFor = Exception.class) public ElectricityCabinetServerOperRecord insert(
        ElectricityCabinetServerOperRecord electricityCabinetServerOperRecord) {
        this.electricityCabinetServerOperRecordMapper.insertOne(electricityCabinetServerOperRecord);
        return electricityCabinetServerOperRecord;
    }

    /**
     * 修改数据
     *
     * @param electricityCabinetServerOperRecord 实例对象
     * @return 实例对象
     */
    @Override @Transactional(rollbackFor = Exception.class) public Integer update(
        ElectricityCabinetServerOperRecord electricityCabinetServerOperRecord) {
        return this.electricityCabinetServerOperRecordMapper.update(electricityCabinetServerOperRecord);

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override @Transactional(rollbackFor = Exception.class) public Boolean deleteById(Long id) {
        return this.electricityCabinetServerOperRecordMapper.deleteById(id) > 0;
    }

    @Override public R queryList(String createUserName, Long eleServerId, Long offset, Long size) {
        List<ElectricityCabinetServerOperRecordVo> data =
            electricityCabinetServerOperRecordMapper.queryList(createUserName, eleServerId, offset, size);

        Long count = electricityCabinetServerOperRecordMapper.queryCount(createUserName, eleServerId);
        return R.ok(new PageDataAndCountVo<>(data, count));
    }
}
