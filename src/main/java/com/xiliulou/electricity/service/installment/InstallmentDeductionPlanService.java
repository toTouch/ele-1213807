package com.xiliulou.electricity.service.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.query.installment.InstallmentDeductionPlanQuery;
import com.xiliulou.electricity.vo.installment.InstallmentDeductionPlanAssemblyVO;

import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:52
 */
public interface InstallmentDeductionPlanService {
    
    /**
     * 新增单条数据
     *
     * @param installmentDeductionPlan 数据库表实体类对象
     * @return 保存的数据条数
     */
    Integer insert(InstallmentDeductionPlan installmentDeductionPlan);
    
    /**
     * 新增单条数据
     *
     * @param installmentDeductionPlans 数据库表实体类对象
     * @return 保存的数据条数
     */
    Integer batchInsert(List<InstallmentDeductionPlan> installmentDeductionPlans);
    
    /**
     * 更新单条数据
     *
     * @param installmentDeductionPlan 数据库表实体类对象
     * @return 更新操作影响的数据行数
     */
    Integer update(InstallmentDeductionPlan installmentDeductionPlan);
    
    /**
     * 后台根据签约记录查询代扣计划
     *
     * @param query 查询请求对象
     */
    R<List<InstallmentDeductionPlanAssemblyVO>> listDeductionPlanForRecordAdmin(InstallmentDeductionPlanQuery query);
    
    /**
     * 根据分期套餐签约记录查询代扣计划
     *
     * @param query 请求签约号
     * @return 代扣计划集合
     */
    R<List<InstallmentDeductionPlan>> listDeductionPlanByAgreementNo(InstallmentDeductionPlanQuery query);
    
    /**
     * 生成代扣计划
     *
     * @param installmentRecord 签约记录
     * @return 代扣计划生成结果
     */
    List<InstallmentDeductionPlan> generateDeductionPlan(InstallmentRecord installmentRecord);
    
    /**
     * 查询可代扣的请求签约号
     *
     * @param time 当前时间
     * @return 请求签约号集合
     */
    List<String> listExternalAgreementNoForDeduct(Long time);
    
    /**
     * 根据请求签约号和期数查询还款计划
     *
     * @param tenantId            租户id
     * @param externalAgreementNo 请求签约号
     * @param issue               期数
     * @return 还款计划
     */
    List<InstallmentDeductionPlan> listByExternalAgreementNoAndIssue(Integer tenantId, String externalAgreementNo, Integer issue);
    
    /**
     * 根据id查询代扣计划
     *
     * @param id 主键id
     * @return 代扣计划
     */
    InstallmentDeductionPlan queryById(Long id);
    
    /**
     * 根据payNo查找代扣计划
     *
     * @param tenantId            租户id
     * @param externalAgreementNo 请求签约号
     * @param payNo               扣款订单号
     * @return 代扣计划
     */
    InstallmentDeductionPlan queryByPayNo(Integer tenantId, String externalAgreementNo, String payNo);
}
