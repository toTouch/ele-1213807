package com.xiliulou.electricity.mapper.car;

import com.xiliulou.electricity.entity.car.CarRentalPackageDepositRefundPO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositRefundQryModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 租车套餐押金退款表 Mapper
 *
 * @author xiaohui.song
 **/
@Mapper
public interface CarRentalPackageDepositRefundMapper {

    /**
     * 根据押金缴纳订单编码，查询最后一笔的退押订单信息
     *
     * @param depositPayOrderNo 押金缴纳编码
     * @return 押金退款订单编码
     */
    CarRentalPackageDepositRefundPO selectLastByDepositPayOrderNo(String depositPayOrderNo);

    /**
     * 根据退押申请单编码进行更新
     * @param entity 数据实体
     * @return
     */
    int updateByOrderNo(CarRentalPackageDepositRefundPO entity);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询条件模型
     * @return
     */
    List<CarRentalPackageDepositRefundPO> list(CarRentalPackageDepositRefundQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询条件模型
     * @return
     */
    List<CarRentalPackageDepositRefundPO> page(CarRentalPackageDepositRefundQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询条件模型
     * @return
     */
    Integer count(CarRentalPackageDepositRefundQryModel qryModel);

    /**
     * 根据订单编码查询
     * @param orderNo 订单编码
     * @return
     */
    CarRentalPackageDepositRefundPO selectByOrderNo(String orderNo);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return
     */
    CarRentalPackageDepositRefundPO selectById(Long id);

    /**
     * 插入
     * @param entity 实体类
     * @return
     */
    int insert(CarRentalPackageDepositRefundPO entity);
}
