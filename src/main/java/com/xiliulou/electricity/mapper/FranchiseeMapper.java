package com.xiliulou.electricity.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.query.FranchiseeQuery;
import com.xiliulou.electricity.vo.FranchiseeVO;
import com.xiliulou.electricity.vo.SearchVo;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (ElectricityBatteryBind)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
public interface FranchiseeMapper extends BaseMapper<Franchisee> {

    List<FranchiseeVO> queryList( @Param("query") FranchiseeQuery franchiseeQuery);

	Integer queryCount( @Param("query") FranchiseeQuery franchiseeQuery);

    Franchisee queryByElectricityBatteryId(Long id);

    Franchisee queryByUserId(Long uid);

    Franchisee queryByCabinetId(@Param("id") Integer id, @Param("tenantId") Integer tenantId);

    Integer update( Franchisee franchisee );

    List<Franchisee> selectListByQuery(FranchiseeQuery franchiseeQuery);

    int editFranchisee(Franchisee updateFranchisee);

    List<SearchVo> search(FranchiseeQuery franchiseeQuery);
}
