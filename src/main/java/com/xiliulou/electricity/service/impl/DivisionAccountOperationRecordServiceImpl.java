package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.DivisionAccountConfig;
import com.xiliulou.electricity.entity.DivisionAccountOperationRecord;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.DivisionAccountOperationRecordMapper;
import com.xiliulou.electricity.service.DivisionAccountConfigService;
import com.xiliulou.electricity.service.DivisionAccountOperationRecordService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.vo.DivisionAccountOperationRecordVO;
import com.xiliulou.electricity.vo.UserInfoVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * (DivisionAccountOperationRecord)�����ʵ����
 *
 * @author zhangyanbo
 * @since 2023-05-08 13:38:49
 */
@Service("divisionAccountOperationRecordService")
@Slf4j
public class DivisionAccountOperationRecordServiceImpl implements DivisionAccountOperationRecordService {
    @Resource
    private DivisionAccountOperationRecordMapper divisionAccountOperationRecordMapper;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private DivisionAccountConfigService divisionAccountConfigService;

    /**
     * ͨ��ID��ѯ�������ݴ�DB
     *
     * @param id ����
     * @return ʵ������
     */
    @Override
    public DivisionAccountOperationRecord queryByIdFromDB(Long id) {
        return this.divisionAccountOperationRecordMapper.queryById(id);
    }

    /**
     * ͨ��ID��ѯ�������ݴӻ���
     *
     * @param id ����
     * @return ʵ������
     */
    @Override
    public DivisionAccountOperationRecord queryByIdFromCache(Long id) {
        return null;
    }


    /**
     * ��ѯ��������
     *
     * @param offset ��ѯ��ʼλ��
     * @param limit  ��ѯ����
     * @return �����б�
     */
    @Override
    public List<DivisionAccountOperationRecord> queryAllByLimit(int offset, int limit) {
        return this.divisionAccountOperationRecordMapper.queryAllByLimit(offset, limit);
    }

    /**
     * ��������
     *
     * @param divisionAccountOperationRecord ʵ������
     * @return ʵ������
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DivisionAccountOperationRecord insert(DivisionAccountOperationRecord divisionAccountOperationRecord) {
        this.divisionAccountOperationRecordMapper.insertOne(divisionAccountOperationRecord);
        return divisionAccountOperationRecord;
    }

    /**
     * �޸�����
     *
     * @param divisionAccountOperationRecord ʵ������
     * @return ʵ������
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(DivisionAccountOperationRecord divisionAccountOperationRecord) {
        return this.divisionAccountOperationRecordMapper.update(divisionAccountOperationRecord);

    }

    /**
     * ͨ������ɾ������
     *
     * @param id ����
     * @return �Ƿ�ɹ�
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.divisionAccountOperationRecordMapper.deleteById(id) > 0;
    }

    @Override
    public List<DivisionAccountOperationRecordVO> queryList(DivisionAccountOperationRecord divisionAccountOperationRecord) {

        List<DivisionAccountOperationRecord> divisionAccountOperationRecords = divisionAccountOperationRecordMapper.queryList(divisionAccountOperationRecord);
        if (CollectionUtils.isEmpty(divisionAccountOperationRecords)) {
            return Collections.emptyList();
        }

        return divisionAccountOperationRecords.parallelStream().map(item -> {
            DivisionAccountOperationRecordVO divisionAccountOperationRecordVO = new DivisionAccountOperationRecordVO();
            BeanUtils.copyProperties(item, divisionAccountOperationRecordVO);

            UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
            divisionAccountOperationRecordVO.setUserName(Objects.nonNull(userInfo) ? userInfo.getUserName() : "");


            DivisionAccountConfig divisionAccountConfig = divisionAccountConfigService.queryByIdFromCache(item.getDivisionAccountId().longValue());
            divisionAccountOperationRecordVO.setHierarchy(divisionAccountConfig.getHierarchy());
            divisionAccountOperationRecordVO.setStatus(divisionAccountConfig.getStatus());
            divisionAccountOperationRecordVO.setType(divisionAccountConfig.getType());
            return divisionAccountOperationRecordVO;
        }).collect(Collectors.toList());
    }


    @Override
    @Slave
    public Integer queryCount(DivisionAccountOperationRecord operationRecord) {
        return this.divisionAccountOperationRecordMapper.queryCount(operationRecord);
    }
}
