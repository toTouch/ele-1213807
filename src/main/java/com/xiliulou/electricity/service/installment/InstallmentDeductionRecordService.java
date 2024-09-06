package com.xiliulou.electricity.service.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FyConfig;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionRecord;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.query.installment.InstallmentDeductionRecordQuery;
import com.xiliulou.electricity.vo.installment.InstallmentDeductionRecordVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:55
 */
public interface InstallmentDeductionRecordService {
    
    /**
     * 新增单条数据
     *
     * @param installmentDeductionRecord 数据库表实体类对象
     * @return 保存的数据条数
     */
    Integer insert(InstallmentDeductionRecord installmentDeductionRecord);
    
    /**
     * 更新单条数据
     *
     * @param installmentDeductionRecord 数据库表实体类对象
     * @return 更新操作影响的数据行数
     */
    Integer update(InstallmentDeductionRecord installmentDeductionRecord);
    
    /**
     * 分页查询数据
     *
     * @param installmentDeductionRecordQuery 分页查询请求参数
     * @return 分页数据VO结果
     */
    R<List<InstallmentDeductionRecordVO>> listForPage(InstallmentDeductionRecordQuery installmentDeductionRecordQuery);
    
    /**
     * 查询分页总数
     *
     * @param installmentDeductionRecordQuery 分页查询请求参数
     * @return 分页总数
     */
    R<Integer> count(InstallmentDeductionRecordQuery installmentDeductionRecordQuery);
    
    /**
     * 调起代扣
     *
     * @param deductionPlan     代扣计划
     * @param installmentRecord 签约记录
     * @param fyConfig          蜂云配置
     * @return 调起结果
     */
    Triple<Boolean, String, Object> initiatingDeduct(InstallmentDeductionPlan deductionPlan, InstallmentRecord installmentRecord, FyConfig fyConfig);
    
    /**
     * 代扣回调方法
     *
     * @param bizContent 业务参数
     * @param uid        签约用户
     * @return 返回回调响应结果
     */
    String agreementPayNotify(String bizContent, Long uid);
    
    /**
     * 根据资金处理订单号查询代扣记录
     *
     * @param payNo 资金处理订单号
     * @return 代扣记录
     */
    InstallmentDeductionRecord queryByPayNo(String payNo);
    
    /**
     * 代扣定时任务
     */
    void dailyInstallmentDeduct();

    R queryStatus(String payNo);
}
