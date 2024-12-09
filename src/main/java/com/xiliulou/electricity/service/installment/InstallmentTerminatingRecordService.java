package com.xiliulou.electricity.service.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.entity.installment.InstallmentTerminatingRecord;
import com.xiliulou.electricity.query.installment.InstallmentTerminatingRecordQuery;
import com.xiliulou.electricity.vo.installment.InstallmentTerminatingRecordVO;

import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/28 10:52
 */
public interface InstallmentTerminatingRecordService {
    
    /**
     * 新增单条数据
     *
     * @param installmentTerminatingRecord 数据库表实体类对象
     * @return 保存的数据条数
     */
    Integer insert(InstallmentTerminatingRecord installmentTerminatingRecord);
    
    /**
     * 更新单条数据
     *
     * @param installmentTerminatingRecord 数据库表实体类对象
     * @return 更新操作影响的数据行数
     */
    Integer update(InstallmentTerminatingRecord installmentTerminatingRecord);
    
    /**
     * 查询分页总数
     *
     * @param query 分页查询请求参数
     * @return 分页数据VO结果
     */
    R<List<InstallmentTerminatingRecordVO>> listForPage(InstallmentTerminatingRecordQuery query);
    
    /**
     * 查询分页总数
     *
     * @param query 分页查询请求参数
     * @return 分页总数
     */
    R<Integer> count(InstallmentTerminatingRecordQuery query);
    
    /**
     * 根据状态查询签约记录对应的解约申请
     *
     * @param query 参数
     * @return 返回解约申请
     */
    List<InstallmentTerminatingRecord> listForRecordWithStatus(InstallmentTerminatingRecordQuery query);
    
    /**
     * 生成解约记录
     *
     * @param installmentRecord 签约记录
     * @param reason            原因
     * @return 返回解约记录
     */
    InstallmentTerminatingRecord generateTerminatingRecord(InstallmentRecord installmentRecord, String reason, Boolean completedOrNot);
    
    
    /**
     * 根据id查询解约申请记录
     *
     * @param id 解约记录id
     * @return 解约记录
     */
    InstallmentTerminatingRecord queryById(Long id);
    
    /**
     * 根据请求签约号查询最新解约记录
     *
     * @param externalAgreementNo 请求签约号
     * @return 解约记录
     */
    InstallmentTerminatingRecord queryLatestByExternalAgreementNo(String externalAgreementNo);
    
    /**
     * 根据状态查询用户的解约申请
     *
     * @param query 参数
     * @return 返回解约申请
     */
    List<InstallmentTerminatingRecord> listForUserWithStatus(InstallmentTerminatingRecordQuery query);
}
