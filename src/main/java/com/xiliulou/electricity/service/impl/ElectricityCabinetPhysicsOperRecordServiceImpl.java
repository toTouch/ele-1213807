package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetPhysicsOperRecord;
import com.xiliulou.electricity.mapper.ElectricityCabinetPhysicsOperRecordMapper;
import com.xiliulou.electricity.service.ElectricityCabinetPhysicsOperRecordService;
import com.xiliulou.electricity.vo.ElectricityCabinetPhysicsOperRecordVo;
import com.xiliulou.electricity.vo.PageDataAndCountVo;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
/**
 * (ElectricityCabinetPhysicsOperRecord)表服务实现类
 *
 * @author Hardy
 * @since 2022-08-16 15:31:13
 */
@Service("electricityCabinetPhysicsOperRecordService")
@Slf4j
public class ElectricityCabinetPhysicsOperRecordServiceImpl implements ElectricityCabinetPhysicsOperRecordService {
    @Resource
    private ElectricityCabinetPhysicsOperRecordMapper electricityCabinetPhysicsOperRecordMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetPhysicsOperRecord queryByIdFromDB(Long id) {
        return this.electricityCabinetPhysicsOperRecordMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public  ElectricityCabinetPhysicsOperRecord queryByIdFromCache(Long id) {
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
    public List<ElectricityCabinetPhysicsOperRecord> queryAllByLimit(int offset, int limit) {
        return this.electricityCabinetPhysicsOperRecordMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param electricityCabinetPhysicsOperRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElectricityCabinetPhysicsOperRecord insert(ElectricityCabinetPhysicsOperRecord electricityCabinetPhysicsOperRecord) {
        this.electricityCabinetPhysicsOperRecordMapper.insertOne(electricityCabinetPhysicsOperRecord);
        return electricityCabinetPhysicsOperRecord;
    }

    /**
     * 修改数据
     *
     * @param electricityCabinetPhysicsOperRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElectricityCabinetPhysicsOperRecord electricityCabinetPhysicsOperRecord) {
       return this.electricityCabinetPhysicsOperRecordMapper.update(electricityCabinetPhysicsOperRecord);
         
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
        return this.electricityCabinetPhysicsOperRecordMapper.deleteById(id) > 0;
    }

    @Override
    public R electricityCabinetOperRecordList(Integer size, Integer offset, Integer eleId, Integer operateType, Long beginTime, Long endTime, Integer cellNo, String userName, String phone) {
        List<ElectricityCabinetPhysicsOperRecordVo> data = electricityCabinetPhysicsOperRecordMapper.electricityCabinetOperRecordList(size, offset, eleId, operateType, beginTime, endTime, cellNo, userName, phone);
        Long count = electricityCabinetPhysicsOperRecordMapper.electricityCabinetOperRecordCount(eleId, operateType, beginTime, endTime, cellNo, userName, phone);

        PageDataAndCountVo<List<ElectricityCabinetPhysicsOperRecordVo>> vo = new PageDataAndCountVo<>();
        vo.setData(data);
        vo.setCount(count);
        return R.ok(vo);
    }
}
