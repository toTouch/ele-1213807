package com.xiliulou.electricity.service.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.query.installment.InstallmentPayQuery;
import com.xiliulou.electricity.query.installment.InstallmentRecordQuery;
import com.xiliulou.electricity.query.installment.InstallmentSignQuery;
import com.xiliulou.electricity.vo.installment.InstallmentRecordVO;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:50
 */
public interface InstallmentRecordService {
    
    /**
     * 新增单条数据
     * @param installmentRecord 数据库表实体类对象
     * @return 保存的数据条数
     */
    Integer insert(InstallmentRecord installmentRecord);
    
    /**
     * 更新单条数据
     * @param installmentRecord 数据库表实体类对象
     * @return 更新操作影响的数据行数
     */
    Integer update(InstallmentRecord installmentRecord);
    
    /**
     * 分页查询数据
     * @param installmentRecordQuery 分页查询请求参数
     * @return 分页数据VO结果
     */
    R<List<InstallmentRecordVO>> listForPage(InstallmentRecordQuery installmentRecordQuery);
    
    /**
     * 查询分页总数
     * @param installmentRecordQuery 分页查询请求参数
     * @return 分页总数
     */
    R<Integer> count(InstallmentRecordQuery installmentRecordQuery);
    
    /**
     * 生成签约记录，
     * @param query 购买请求对象
     * @param userInfo 用户信息
     * @return 生成分期签约记录的结果
     */
    Triple<Boolean, String, InstallmentRecord> generateInstallmentRecord(InstallmentPayQuery query, BatteryMemberCard batteryMemberCard, CarRentalPackagePo carRentalPackagePo, UserInfo userInfo);

    /**
     * 请求峰云签约接口返回二维码链接
     */
    R<Object> sign(InstallmentSignQuery query, HttpServletRequest request);
    
    /**
     * 根据状态查询用户的最近一条签约记录
     * @param uid 用户
     * @param status 签约记录状态
     * @return 签约记录
     */
    InstallmentRecord queryRecordWithStatusForUser(Long uid, List<Integer> status);
    
    /**
     * 签约回调方法
     * @param bizContent 业务参数
     * @param uid 业务请求用户
     * @return 返回成功响应
     */
    String signNotify(String bizContent, Long uid);
    
    /**
     * 根据请求签约号查询签约记录
     * @param externalAgreementNo 请求签约号
     * @return 返回签约记录
     */
    InstallmentRecord queryByExternalAgreementNo(String externalAgreementNo);

    R<InstallmentRecordVO> queryInstallmentRecordForUser();
}
