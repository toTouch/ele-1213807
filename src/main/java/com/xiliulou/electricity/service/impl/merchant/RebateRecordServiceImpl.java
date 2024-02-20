package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.electricity.entity.merchant.RebateRecord;
import com.xiliulou.electricity.mapper.merchant.RebateRecordMapper;
import com.xiliulou.electricity.service.merchant.RebateRecordService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (RebateRecord)表服务实现类
 *
 * @author zzlong
 * @since 2024-02-20 14:31:51
 */
@Service("rebateRecordService")
@Slf4j
public class RebateRecordServiceImpl implements RebateRecordService {
    
    @Resource
    private RebateRecordMapper rebateRecordMapper;

    @Override
    public RebateRecord queryById(Long id) {
        return this.rebateRecordMapper.selectById(id);
    }

    @Override
    public RebateRecord insert(RebateRecord rebateRecord) {
        this.rebateRecordMapper.insertOne(rebateRecord);
        return rebateRecord;
    }
    
    /**
     * 修改数据
     *
     * @param rebateRecord 实例对象
     * @return 实例对象
     */
    @Override
    public Integer updateById(RebateRecord rebateRecord) {
        return this.rebateRecordMapper.update(rebateRecord);
    }

    @Override
    public Integer deleteById(Long id) {
        return this.rebateRecordMapper.deleteById(id);
    }
}
