package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.CarMemberCardOrder;
import com.xiliulou.electricity.entity.DivisionAccountRecord;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.query.DivisionAccountRecordQuery;
import com.xiliulou.electricity.vo.DivisionAccountRecordStatisticVO;
import com.xiliulou.electricity.vo.DivisionAccountRecordVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (DivisionAccountRecord)表服务接口
 *
 * @author zzlong
 * @since 2023-04-24 16:23:44
 */
public interface DivisionAccountRecordService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    DivisionAccountRecord queryByIdFromDB(Long id);

    List<DivisionAccountRecordVO> selectByPage(DivisionAccountRecordQuery query);

    Integer selectByPageCount(DivisionAccountRecordQuery query);

    /**
     * 新增数据
     *
     * @param divisionAccountRecord 实例对象
     * @return 实例对象
     */
    DivisionAccountRecord insert(DivisionAccountRecord divisionAccountRecord);

    /**
     * 修改数据
     *
     * @param divisionAccountRecord 实例对象
     * @return 实例对象
     */
    Integer update(DivisionAccountRecord divisionAccountRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    List<DivisionAccountRecordStatisticVO> selectStatisticByPage(DivisionAccountRecordQuery query);

    Integer selectStatisticByPageCount(DivisionAccountRecordQuery query);

    void handleBatteryMembercardDivisionAccount(ElectricityMemberCardOrder electricityMemberCardOrder);

    void handleCarMembercardDivisionAccount(CarMemberCardOrder carMemberCardOrder);
    
    Triple<Boolean, String, Object> divisionAccountCompensation(String orderId, Integer type);
}
