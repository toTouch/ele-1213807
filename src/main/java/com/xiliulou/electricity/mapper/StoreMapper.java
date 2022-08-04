package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.vo.MapVo;
import com.xiliulou.electricity.vo.StoreVO;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
 * 门店表(TStore)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
public interface StoreMapper extends BaseMapper<Store> {


    /**
     * 查询指定行数据
     */
    List<StoreVO> queryList(@Param("query") StoreQuery storeQuery);

	Integer queryCount(@Param("query") StoreQuery storeQuery);

    List<StoreVO> showInfoByDistance(@Param("query") StoreQuery storeQuery);

    Integer homeOne(@Param("storeIdList") List<Long> storeIdList,@Param("tenantId") Integer tenantId);

	List<HashMap<String, String>> homeThree(@Param("startTimeMilliDay") long startTimeMilliDay, @Param("endTimeMilliDay") Long endTimeMilliDay,@Param("storeIdList") List<Long> storeIdList ,@Param("tenantId")Integer tenantId);

    List<MapVo> queryCountGroupByProvinceId(@Param("tenantId") Integer tenantId);

    List<Long> queryStoreIdsByProvinceId(@Param("tenantId") Integer tenantId,@Param("pid") Integer pid,@Param("cid") Integer cid);

    List<MapVo> queryCountGroupByCityId(@Param("tenantId") Integer tenantId,@Param("pid") Integer pid);

    List<Long> queryStoreIdByFranchiseeId(@Param("franchiseeId")Long franchiseeId);
}
