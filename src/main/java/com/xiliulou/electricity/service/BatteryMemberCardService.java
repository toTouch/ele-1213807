package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.BatteryMemberCardStatusQuery;
import com.xiliulou.electricity.query.BatteryModelQuery;
import com.xiliulou.electricity.vo.BatteryMemberCardSearchVO;
import com.xiliulou.electricity.vo.BatteryMemberCardVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (BatteryMemberCard)表服务接口
 *
 * @author zzlong
 * @since 2023-07-07 14:06:31
 */
public interface BatteryMemberCardService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryMemberCard queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryMemberCard queryByIdFromCache(Long id);

    /**
     * 修改数据
     *
     * @param batteryMemberCard 实例对象
     * @return 实例对象
     */
    Integer update(BatteryMemberCard batteryMemberCard);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Integer deleteById(Long id);

    List<BatteryMemberCardVO> selectByPage(BatteryMemberCardQuery query);

    Integer selectByPageCount(BatteryMemberCardQuery query);

    List<BatteryMemberCardSearchVO> search(BatteryMemberCardQuery query);

    Triple<Boolean, String, Object> updateStatus(BatteryMemberCardStatusQuery batteryModelQuery);

    Triple<Boolean, String, Object> delete(Long id);

    Triple<Boolean, String, Object> modify(BatteryMemberCardQuery query);

    Triple<Boolean, String, Object> save(BatteryMemberCardQuery query);

    List<BatteryMemberCardVO> selectByPageForUser(BatteryMemberCardQuery query);

    List<String> selectMembercardBatteryV(BatteryMemberCardQuery query);
}
