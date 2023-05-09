package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.DivisionAccountOperationRecord;
import com.xiliulou.electricity.query.DivisionAccountOperationRecordQuery;
import com.xiliulou.electricity.vo.DivisionAccountOperationRecordVO;

import java.util.List;

/**
 * (DivisionAccountOperationRecord)�����ӿ�
 *
 * @author zhangyanbo
 * @since 2023-05-08 13:38:49
 */
public interface DivisionAccountOperationRecordService {

    /**
     * ͨ��ID��ѯ�������ݴ����ݿ�
     *
     * @param id ����
     * @return ʵ������
     */
    DivisionAccountOperationRecord queryByIdFromDB(Long id);

    /**
     * ͨ��ID��ѯ�������ݴӻ���
     *
     * @param id ����
     * @return ʵ������
     */
    DivisionAccountOperationRecord queryByIdFromCache(Long id);

    /**
     * ��ѯ��������
     *
     * @param offset ��ѯ��ʼλ��
     * @param limit  ��ѯ����
     * @return �����б�
     */
    List<DivisionAccountOperationRecord> queryAllByLimit(int offset, int limit);

    /**
     * ��������
     *
     * @param divisionAccountOperationRecord ʵ������
     * @return ʵ������
     */
    DivisionAccountOperationRecord insert(DivisionAccountOperationRecord divisionAccountOperationRecord);

    /**
     * �޸�����
     *
     * @param divisionAccountOperationRecord ʵ������
     * @return ʵ������
     */
    Integer update(DivisionAccountOperationRecord divisionAccountOperationRecord);

    /**
     * ͨ������ɾ������
     *
     * @param id ����
     * @return �Ƿ�ɹ�
     */
    Boolean deleteById(Long id);

    List<DivisionAccountOperationRecordVO> queryList (DivisionAccountOperationRecordQuery divisionAccountOperationRecord);


    Integer queryCount(DivisionAccountOperationRecordQuery divisionAccountOperationRecord);

}
