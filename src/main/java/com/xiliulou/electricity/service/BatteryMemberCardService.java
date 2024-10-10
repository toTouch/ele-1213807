package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.BatteryMemberCardStatusQuery;
import com.xiliulou.electricity.query.MemberCardAndCarRentalPackageSortParamQuery;
import com.xiliulou.electricity.vo.BatteryMemberCardSearchVO;
import com.xiliulou.electricity.vo.BatteryMemberCardVO;
import com.xiliulou.security.bean.TokenUser;
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
    
    Integer insert(BatteryMemberCard batteryMemberCard);
    
    Integer insertBatteryMemberCardAndBatteryType(BatteryMemberCard batteryMemberCard, List<String> batteryModels);
    
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
    
    List<BatteryMemberCardVO> selectCarRentalAndElectricityPackages(CarRentalPackageQryModel qryModel);
    
    List<BatteryMemberCardSearchVO> searchV2(BatteryMemberCardQuery query);
    
    List<BatteryMemberCardVO> selectByQuery(BatteryMemberCardQuery query);
    
    Triple<Boolean, String, Object> updateStatus(BatteryMemberCardStatusQuery batteryModelQuery);
    
    Triple<Boolean, String, Object> delete(Long id);
    
    Triple<Boolean, String, Object> modify(BatteryMemberCardQuery query);
    
    Triple<Boolean, String, Object> save(BatteryMemberCardQuery query);
    
    Long transformBatteryMembercardEffectiveTime(BatteryMemberCard batteryMemberCard, ElectricityMemberCardOrder memberCardOrder);
    
    Long transformBatteryMembercardEffectiveTime(BatteryMemberCard batteryMemberCard, Long validDays);
    
    List<BatteryMemberCardVO> selectByPageForUser(BatteryMemberCardQuery query);
    
    List<String> selectMembercardBatteryV(BatteryMemberCardQuery query);
    
    List<BatteryMemberCardVO> selectUserBatteryMembercardList(BatteryMemberCardQuery query);
    
    List<BatteryMemberCardVO> selectListByQuery(BatteryMemberCardQuery query);
    
    List<BatteryMemberCard> selectListByCouponId(Long couponId);
    
    Integer isMemberCardBindFranchinsee(Long id, Integer tenantId);
    
    List<BatteryMemberCardVO> selectByPageForMerchant(BatteryMemberCardQuery query);
    
    List<BatteryMemberCard> listMemberCardsByIdList(BatteryMemberCardQuery query);
    
    /**
     * 批量修改套餐排序参数
     *
     * @param sortParamQueries 套餐id、排序参数
     * @return 修改行数
     */
    Integer batchUpdateSortParam(List<MemberCardAndCarRentalPackageSortParamQuery> sortParamQueries);
    
    /**
     * 查询套餐以供后台排序
     *
     * @param tokenUser ContextHolder中保存的用户信息
     * @return 返回id、name、sortParam、createTime
     */
    List<BatteryMemberCardVO> listMemberCardForSort(TokenUser tokenUser);
    
    List<BatteryMemberCardVO> listSuperAdminPage(BatteryMemberCardQuery query);
    
    /**
     * 检查用户与套餐的用户分组是否匹配
     * @param uid 用户uid
     * @param franchiseeId 加盟商id
     * @param memberCard 套餐
     * @param source 区分用户端与后台
     * @return 检查结果
     */
    Triple<Boolean, String, Object> checkUserInfoGroupWithMemberCard(Long uid, Long franchiseeId, BatteryMemberCard memberCard, Integer source);
}
