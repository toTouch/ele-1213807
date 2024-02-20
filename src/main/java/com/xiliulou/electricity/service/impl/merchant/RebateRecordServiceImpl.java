package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.merchant.RebateRecord;
import com.xiliulou.electricity.mapper.merchant.RebateRecordMapper;
import com.xiliulou.electricity.request.merchant.RebateRecordRequest;
import com.xiliulou.electricity.service.merchant.RebateRecordService;
import com.xiliulou.electricity.vo.merchant.RebateRecordVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

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
    
    @Override
    public RebateRecord queryByOrderId(String orderId) {
        return this.rebateRecordMapper.selectByOrderId(orderId);
    }
    
    @Slave
    @Override
    public List<RebateRecordVO> listByPage(RebateRecordRequest query) {
        List<RebateRecord> list=this.rebateRecordMapper.selectByPage(query);
        if(CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        
        return list.stream().map(item->{
            RebateRecordVO rebateRecord = new RebateRecordVO();
    
            
            
            return rebateRecord;
    
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public Integer countByPage(RebateRecordRequest query) {
        return this.rebateRecordMapper.selectByPageCount(query);
    }
}
