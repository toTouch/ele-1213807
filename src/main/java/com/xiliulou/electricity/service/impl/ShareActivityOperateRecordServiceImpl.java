package com.xiliulou.electricity.service.impl;

import com.google.api.client.util.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.entity.ShareActivityOperateRecord;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.mapper.ShareActivityOperateRecordMapper;
import com.xiliulou.electricity.query.ShareActivityQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.vo.ShareActivityOperateRecordVO;
import com.xiliulou.electricity.vo.activity.ActivityPackageVO;
import com.xiliulou.electricity.vo.activity.ShareActivityPackageVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * (ShareActivityOperateRecord)表服务实现类
 *
 * @author zzlong
 * @since 2023-05-24 14:47:17
 */
@Service("shareActivityOperateRecordService")
@Slf4j
public class ShareActivityOperateRecordServiceImpl implements ShareActivityOperateRecordService {
    @Resource
    private ShareActivityOperateRecordMapper shareActivityOperateRecordMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private ElectricityMemberCardService electricityMemberCardService;
    @Autowired
    ShareActivityMemberCardService shareActivityMemberCardService;

    @Autowired
    BatteryMemberCardService batteryMemberCardService;

    @Autowired
    private CarRentalPackageService carRentalPackageService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ShareActivityOperateRecord queryByIdFromDB(Long id) {
        return this.shareActivityOperateRecordMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ShareActivityOperateRecord queryByIdFromCache(Long id) {
        return null;
    }

    @Override
    public List<ShareActivityOperateRecordVO> page(ShareActivityQuery query) {
        List<ShareActivityOperateRecord> list=shareActivityOperateRecordMapper.selectByPage(query);
        if(CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }

        return list.parallelStream().map(item -> {
            ShareActivityOperateRecordVO shareActivityOperateRecordVO = new ShareActivityOperateRecordVO();
            BeanUtils.copyProperties(item, shareActivityOperateRecordVO);

            User user = userService.queryByUidFromCache(item.getUid());
            shareActivityOperateRecordVO.setUsername(Objects.nonNull(user) ? user.getName() : "");

            if (StringUtils.isNotBlank(item.getMemberCard())) {
                List<String> packageNames;
                if (isJsonStr(item.getMemberCard())) {
                    //3.0新的处理方式
                    packageNames = getPackageNames(item.getMemberCard());
                } else {
                    //旧的处理方式，只存在换电套餐的情况
                    packageNames = getOldBatteryPackages(item.getMemberCard());
                }
                shareActivityOperateRecordVO.setMembercardNames(packageNames);
            }

            return shareActivityOperateRecordVO;
        }).collect(Collectors.toList());
    }

    /**
     * 获取旧的换电套餐名称信息，针对3.0之前的旧数据。
     * @param packageInfo
     * @return
     */
    private List<String> getOldBatteryPackages(String packageInfo){
        List<String> packageNames = Lists.newArrayList();
        if(StringUtils.isNotEmpty(packageInfo)){
            List<Long> membercardIds = JsonUtil.fromJsonArray(packageInfo, Long.class);
            if (CollectionUtils.isNotEmpty(membercardIds)) {
                List<ElectricityMemberCard> membercardList = Lists.newArrayList();
                for (Long membercardId : membercardIds) {
                    ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(membercardId.intValue());
                    if (Objects.nonNull(electricityMemberCard)) {
                        membercardList.add(electricityMemberCard);
                    }
                }

                if (CollectionUtils.isNotEmpty(membercardList)) {
                    packageNames = membercardList.stream().map(ElectricityMemberCard::getName).collect(Collectors.toList());
                }
            }
        }

        return packageNames;
    }

    private boolean isJsonStr(String str){
        boolean isJSONString = false;
        Gson gson = new Gson();
        try {
            JsonElement jsonElement = gson.fromJson(str, JsonElement.class);
            isJSONString = jsonElement.isJsonObject();
        } catch (JsonSyntaxException ex) {
            log.info("This string is not json str, {}", str);
        }

        return isJSONString;
    }

    private List<String> getPackageNames(String packageInfo){
        ShareActivityPackageVO shareActivityPackageVO = JsonUtil.fromJson(packageInfo, ShareActivityPackageVO.class);
        List<ActivityPackageVO> activityPackageVOList = shareActivityPackageVO.getPackages();
        List<String> packageNames = Lists.newArrayList();
        for(ActivityPackageVO activityPackageVO : activityPackageVOList){
            Long packageId = activityPackageVO.getPackageId();
            Integer packageType = activityPackageVO.getPackageType();
            if(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode().equals(packageType)){
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(packageId);
                if(Objects.nonNull(batteryMemberCard)){
                    packageNames.add(batteryMemberCard.getName());
                }
            }else if(PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode().equals(packageType)
                    || PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode().equals(packageType)){
                CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(packageId);
                if(Objects.nonNull(carRentalPackagePO)){
                    packageNames.add(carRentalPackagePO.getName());
                }
            }
        }

        return packageNames;

    }

    @Override
    public Integer count(ShareActivityQuery query) {
        return shareActivityOperateRecordMapper.selectByPageCount(query);
    }

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<ShareActivityOperateRecord> queryAllByLimit(int offset, int limit) {
        return this.shareActivityOperateRecordMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param shareActivityOperateRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareActivityOperateRecord insert(ShareActivityOperateRecord shareActivityOperateRecord) {
        this.shareActivityOperateRecordMapper.insertOne(shareActivityOperateRecord);
        return shareActivityOperateRecord;
    }

    /**
     * 修改数据
     *
     * @param shareActivityOperateRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ShareActivityOperateRecord shareActivityOperateRecord) {
        return this.shareActivityOperateRecordMapper.update(shareActivityOperateRecord);

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.shareActivityOperateRecordMapper.deleteById(id) > 0;
    }
}
