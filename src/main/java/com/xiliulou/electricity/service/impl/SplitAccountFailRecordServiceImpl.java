package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.SplitAccountFailRecord;
import com.xiliulou.electricity.mapper.SplitAccountFailRecordMapper;
import com.xiliulou.electricity.service.SplitAccountFailRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * (SplitAccountFailRecord)表服务实现类
 *
 * @author makejava
 * @since 2021-05-07 08:10:06
 */
@Service("splitAccountFailRecordService")
@Slf4j
public class SplitAccountFailRecordServiceImpl implements SplitAccountFailRecordService {
    @Resource
    private SplitAccountFailRecordMapper splitAccountFailRecordMapper;

    /**
     * 新增数据
     *
     * @param splitAccountFailRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SplitAccountFailRecord insert(SplitAccountFailRecord splitAccountFailRecord) {
        this.splitAccountFailRecordMapper.insert(splitAccountFailRecord);
        return splitAccountFailRecord;
    }

}
