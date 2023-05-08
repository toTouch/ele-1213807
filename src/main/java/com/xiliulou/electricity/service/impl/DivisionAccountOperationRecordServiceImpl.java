package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.DivisionAccountOperationRecord;
import com.xiliulou.electricity.mapper.DivisionAccountOperationRecordMapper;
import com.xiliulou.electricity.service.DivisionAccountOperationRecordService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

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
    public R queryList(DivisionAccountOperationRecord divisionAccountOperationRecord) {
        return null;
    }
}
