package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ShareMoneyActivityPackage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: Kenneth
 * @Date: 2023/7/31 14:38
 * @Description:
 */
public interface ShareMoneyActivityPackageMapper {

    Integer insertOne(ShareMoneyActivityPackage shareMoneyActivityPackage);

    Integer updateOne(ShareMoneyActivityPackage shareMoneyActivityPackage);

    List<ShareMoneyActivityPackage> selectByQuery(ShareMoneyActivityPackage shareMoneyActivityPackage);

    Integer batchInsertActivityPackages(List<ShareMoneyActivityPackage> activityPackages);

    Integer deleteActivityPackage(@Param("id") Long id);

    List<ShareMoneyActivityPackage> selectActivityPackagesByActivityId(@Param("activityId") Long activityId);

    List<ShareMoneyActivityPackage> selectPackagesByActivityIdAndPackageType(@Param("activityId") Long activityId, @Param("packageType") Integer packageType);

}
