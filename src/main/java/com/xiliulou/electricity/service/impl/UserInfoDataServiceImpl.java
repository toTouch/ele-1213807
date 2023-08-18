package com.xiliulou.electricity.service.impl;

import com.google.common.collect.Lists;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoDataEntity;
import com.xiliulou.electricity.enums.UserInfoDataQueryEnum;
import com.xiliulou.electricity.mapper.ElectricityBatteryMapper;
import com.xiliulou.electricity.mapper.UserBatteryMemberCardMapper;
import com.xiliulou.electricity.mapper.UserInfoMapper;
import com.xiliulou.electricity.query.UserInfoDataQuery;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserInfoDataService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.car.PageDataResult;
import com.xiliulou.electricity.vo.userinfo.UserInfoDataVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 用户运维数据
 */
@Service
@Slf4j
public class UserInfoDataServiceImpl implements UserInfoDataService {
    @Autowired
    private UserDataScopeService userDataScopeService;
    @Autowired
    private TenantService tenantService;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserBatteryMemberCardMapper userBatteryMemberCardMapper;
    @Resource
    private ElectricityBatteryMapper electricityBatteryMapper;
    @Override
    public PageDataResult queryUserInfoData(UserInfoDataQuery userInfoDataQuery) {

        if(checkPageAndDataType(userInfoDataQuery)){
            return new PageDataResult();
        }
        // 统一获取租户ID
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return new PageDataResult();
        }
        userInfoDataQuery.setTenantId(tenantId);
        if(Objects.equals(userInfoDataQuery.getQueryType(), UserInfoDataQueryEnum.ALL.getCode())){
            return queryAllUserInfoDataInfo(userInfoDataQuery);
        }else if(Objects.equals(userInfoDataQuery.getQueryType(), UserInfoDataQueryEnum.EFFECTIVE_USER.getCode())){
            return queryEffectiveUserInfoDataInfo(userInfoDataQuery);
        }else if(Objects.equals(userInfoDataQuery.getQueryType(), UserInfoDataQueryEnum.RENT_USER.getCode())){
            return queryRentUserInfoDataInfo(userInfoDataQuery);
        }else if(Objects.equals(userInfoDataQuery.getQueryType(), UserInfoDataQueryEnum.OVERDUE_USER.getCode())){
            return queryOverdueUserInfoDataInfo(userInfoDataQuery);
        }else if(Objects.equals(userInfoDataQuery.getQueryType(),UserInfoDataQueryEnum.SILENT_USER.getCode())){
            return querySilentUserInfoDataInfo(userInfoDataQuery);
        }
        return new PageDataResult();

    }

    /**
     * 查询所有的用户信息
     * @param userInfoDataQuery
     * @return
     */
    @Slave
    private PageDataResult queryAllUserInfoDataInfo(UserInfoDataQuery userInfoDataQuery) {
        Long offSet = Long.valueOf(userInfoDataQuery.getOffset());
        Long size = Long.valueOf(userInfoDataQuery.getSize());
        Integer tenantId = userInfoDataQuery.getTenantId();
        // 组装查询
        UserInfoQuery userInfoQuery = UserInfoQuery.builder().offset(offSet).size(size).tenantId(tenantId).build();
        // 分页查询用户
        List<UserInfo> userInfoList = userInfoMapper.page(userInfoQuery);
        int count = userInfoMapper.count(userInfoQuery);
        List<UserInfoDataVO> userInfoDataVOList = Lists.newArrayList();
        for(UserInfo userInfo : userInfoList){
            // 填充用户信息
            UserInfoDataVO userInfoDataVO = new UserInfoDataVO();
            userInfoDataVO.setPhone(userInfo.getPhone());
            userInfoDataVO.setUserName(userInfo.getUserName());
            // 查询套餐信息
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardMapper.selectByUid(userInfo.getUid());
            if(userBatteryMemberCard != null){
                userInfoDataVO.setPayCount(userBatteryMemberCard.getCardPayCount());
                userInfoDataVO.setExpireTime(userBatteryMemberCard.getMemberCardExpireTime());
            }
            // 查询电池信息
            String batterySn = electricityBatteryMapper.querySnByUid(userInfo.getUid());
            userInfoDataVO.setBatterySn(StringUtils.isEmpty(batterySn) ? "" : batterySn);

            userInfoDataVOList.add(userInfoDataVO);
        }
        return PageDataResult.result(count, userInfoDataQuery.getSize(), userInfoDataQuery.getOffset(), userInfoDataVOList);
    }

    /**
     * 查询套餐在有效期内的用户
     * @param userInfoDataQuery
     * @return
     */
    @Slave
    private PageDataResult queryEffectiveUserInfoDataInfo(UserInfoDataQuery userInfoDataQuery){
        List<UserInfoDataEntity> userInfoDataEntityList = userInfoMapper.queryEffectiveUserInfoDataByParam(userInfoDataQuery);
        Integer count = userInfoMapper.queryEffectiveUserInfoDataCount(userInfoDataQuery);
        List<UserInfoDataVO> userInfoDataVOList = UserInfoDataVO.entityListToVoList(userInfoDataEntityList);
        for(UserInfoDataVO userInfoDataVO : userInfoDataVOList){
            String batterySn = electricityBatteryMapper.querySnByUid(userInfoDataVO.getUserId());
            userInfoDataVO.setBatterySn(StringUtils.isEmpty(batterySn) ? "" : batterySn);
        }
        return PageDataResult.result(count, userInfoDataQuery.getSize(), userInfoDataQuery.getOffset(), userInfoDataVOList);
    }

    /**
     * 查询在租用户
     * @param userInfoDataQuery
     * @return
     */
    @Slave
    private PageDataResult queryRentUserInfoDataInfo(UserInfoDataQuery userInfoDataQuery){
        List<UserInfoDataEntity> userInfoDataEntityList = userInfoMapper.queryRentUserInfoDataByParam(userInfoDataQuery);
        Integer count = userInfoMapper.queryRentUserInfoDataCount(userInfoDataQuery);
        List<UserInfoDataVO> userInfoDataVOList = UserInfoDataVO.entityListToVoList(userInfoDataEntityList);
        for(UserInfoDataVO userInfoDataVO : userInfoDataVOList){
            UserBatteryMemberCard userBatteryMemberCard =userBatteryMemberCardMapper.selectByUid(userInfoDataVO.getUserId());
            userInfoDataVO.setPayCount(userBatteryMemberCard.getCardPayCount());
            userInfoDataVO.setExpireTime(userBatteryMemberCard.getOrderExpireTime());
        }
        return PageDataResult.result(count, userInfoDataQuery.getSize(), userInfoDataQuery.getOffset(), userInfoDataVOList);
    }

    /**
     * 逾期用户
     * @param userInfoDataQuery
     * @return
     */
    @Slave
    private PageDataResult queryOverdueUserInfoDataInfo(UserInfoDataQuery userInfoDataQuery){
        List<UserInfoDataEntity> userInfoDataEntityList = userInfoMapper.queryOverdueUserInfoDataByParam(userInfoDataQuery);
        Integer count = userInfoMapper.queryOverdueUserInfoDataCount(userInfoDataQuery);
        List<UserInfoDataVO> userInfoDataVOList = UserInfoDataVO.entityListToVoList(userInfoDataEntityList);
        return PageDataResult.result(count, userInfoDataQuery.getSize(), userInfoDataQuery.getOffset(), userInfoDataVOList);
    }

    /**
     * 静默用户
     * @param userInfoDataQuery
     * @return
     */
    @Slave
    private PageDataResult querySilentUserInfoDataInfo(UserInfoDataQuery userInfoDataQuery){
        List<UserInfoDataEntity> userInfoDataEntityList = userInfoMapper.querySilentUserInfoDataByParam(userInfoDataQuery);
        Integer count = userInfoMapper.querySilentUserInfoDataCount(userInfoDataQuery);
        List<UserInfoDataVO> userInfoDataVOList = UserInfoDataVO.entityListToVoList(userInfoDataEntityList);
        return PageDataResult.result(count, userInfoDataQuery.getSize(), userInfoDataQuery.getOffset(), userInfoDataVOList);
    }



    //校验页码和数据类型
    private Boolean checkPageAndDataType(UserInfoDataQuery userInfoDataQuery){
        if(userInfoDataQuery.getOffset() < 0){
            userInfoDataQuery.setOffset(1);
        }
        if(userInfoDataQuery.getSize() > 50 || userInfoDataQuery.getSize() < 0){
            userInfoDataQuery.setSize(10);
        }
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
        }
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return true;
            }
        }
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return true;
        }
        return false;
    }
}
