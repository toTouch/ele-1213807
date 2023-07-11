package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.EleUserEsignRecord;
import com.xiliulou.electricity.mapper.EleUserEsignRecordMapper;
import com.xiliulou.electricity.query.EleUserEsignRecordQuery;
import com.xiliulou.electricity.service.EleUserEsignRecordService;
import com.xiliulou.electricity.vo.EleUserEsignRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: Kenneth
 * @Date: 2023/7/10 11:30
 * @Description:
 */

@Service
@Slf4j
public class EleUserEsignRecordServiceImpl implements EleUserEsignRecordService {

    @Resource
    private EleUserEsignRecordMapper eleUserEsignRecordMapper;

    @Override
    public List<EleUserEsignRecordVO> queryUserEsignRecords(EleUserEsignRecordQuery eleUserEsignRecordQuery) {
        return eleUserEsignRecordMapper.selectByPage(eleUserEsignRecordQuery);
    }

    @Override
    public Integer queryCount(EleUserEsignRecordQuery eleUserEsignRecordQuery) {
        return eleUserEsignRecordMapper.selectByPageCount(eleUserEsignRecordQuery);
    }

}
