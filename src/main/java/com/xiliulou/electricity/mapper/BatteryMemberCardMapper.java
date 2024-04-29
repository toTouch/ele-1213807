package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.MemberCardAndCarRentalPackageSortParamQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseMemberCardQuery;
import com.xiliulou.electricity.vo.BatteryMemberCardAndTypeVO;
import com.xiliulou.electricity.vo.BatteryMemberCardVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (BatteryMemberCard)表数据库访问层
 *
 * @author zzlong
 * @since 2023-07-07 14:06:31
 */
public interface BatteryMemberCardMapper extends BaseMapper<BatteryMemberCard> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryMemberCard queryById(Long id);
    
    /**
     * 修改数据
     *
     * @param batteryMemberCard 实例对象
     * @return 影响行数
     */
    int update(BatteryMemberCard batteryMemberCard);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    List<BatteryMemberCardAndTypeVO> selectByPage(BatteryMemberCardQuery query);
    
    Integer selectByPageCount(BatteryMemberCardQuery query);
    
    List<BatteryMemberCard> selectBySearch(BatteryMemberCardQuery query);
    
    Integer checkMembercardExist(@Param("name") String name, @Param("tenantId") Integer tenantId);
    
    List<BatteryMemberCardAndTypeVO> selectByPageForUser(BatteryMemberCardQuery query);
    
    List<BatteryMemberCardVO> selectMembercardBatteryV(BatteryMemberCardQuery query);
    
    List<BatteryMemberCard> selectByQuery(BatteryMemberCardQuery query);
    
    List<BatteryMemberCardAndTypeVO> selectMemberCardsByEnterprise(EnterpriseMemberCardQuery query);
    
    List<BatteryMemberCardVO> selectMembercardBatteryVByEnterprise(EnterpriseMemberCardQuery query);
    
    Integer isMemberCardBindFranchinsee(@Param("franchiseeId") Long franchiseeId, @Param("tenantId") Integer tenantId);
    
    Integer batchUpdateSortParam(@Param("sortParamQueries") List<MemberCardAndCarRentalPackageSortParamQuery> sortParamQueries);
    
    List<BatteryMemberCardVO> listMemberCardForSort(@Param("tenantId") Integer tenantId);
}
