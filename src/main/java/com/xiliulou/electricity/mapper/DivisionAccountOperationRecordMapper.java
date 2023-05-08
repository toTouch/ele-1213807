package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.DivisionAccountOperationRecord;

import java.util.List;

import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.query.ElectricityCabinetModelQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (DivisionAccountOperationRecord)�����ݿ���ʲ�
 *
 * @author zhangyanbo
 * @since 2023-05-08 13:38:49
 */
public interface DivisionAccountOperationRecordMapper extends BaseMapper<DivisionAccountOperationRecord> {

    /**
     * ͨ��ID��ѯ��������
     *
     * @param id ����
     * @return ʵ������
     */
    DivisionAccountOperationRecord queryById(Long id);

    /**
     * ��ѯָ��������
     *
     * @param offset ��ѯ��ʼλ��
     * @param limit  ��ѯ����
     * @return �����б�
     */
    List<DivisionAccountOperationRecord> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * ͨ��ʵ����Ϊɸѡ������ѯ
     *
     * @param divisionAccountOperationRecord ʵ������
     * @return �����б�
     */
    List<DivisionAccountOperationRecord> queryAll(DivisionAccountOperationRecord divisionAccountOperationRecord);

    /**
     * ��������
     *
     * @param divisionAccountOperationRecord ʵ������
     * @return Ӱ������
     */
    int insertOne(DivisionAccountOperationRecord divisionAccountOperationRecord);

    /**
     * �޸�����
     *
     * @param divisionAccountOperationRecord ʵ������
     * @return Ӱ������
     */
    int update(DivisionAccountOperationRecord divisionAccountOperationRecord);

    /**
     * ͨ������ɾ������
     *
     * @param id ����
     * @return Ӱ������
     */
    int deleteById(Long id);

    /**
     * @return 对象列表
     */
    List <DivisionAccountOperationRecord> queryList(@Param("query") DivisionAccountOperationRecord accountOperationRecord);

}
