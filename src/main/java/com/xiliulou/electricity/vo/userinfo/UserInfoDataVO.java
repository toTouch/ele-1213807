package com.xiliulou.electricity.vo.userinfo;

import com.google.common.collect.Lists;
import com.xiliulou.electricity.entity.UserInfoDataEntity;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * 用户数据运维信息
 */
@Data
public class UserInfoDataVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 激活时间
     */
    private String activeTime;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 电池序列号
     */
    private String batterySn;

    /**
     * 支付次数
     */
    private int payCount;

    /**
     * 会员卡到期时间
     */
    private Long expireTime;

    public static UserInfoDataVO userInfoDataEntityToUserInfoDataVO(UserInfoDataEntity userInfoDataEntity) {
        if (userInfoDataEntity == null) {
            return null;
        }
        UserInfoDataVO userInfoDataVO = new UserInfoDataVO();
        userInfoDataVO.setUserId(userInfoDataEntity.getUid());
        userInfoDataVO.setActiveTime(userInfoDataEntity.getActiveTime());
        userInfoDataVO.setPhone(userInfoDataEntity.getPhone());
        userInfoDataVO.setUserName(userInfoDataEntity.getUserName());
        userInfoDataVO.setBatterySn(userInfoDataEntity.getBatterySn());
        userInfoDataVO.setPayCount(userInfoDataEntity.getPayCount());
        userInfoDataVO.setExpireTime(userInfoDataEntity.getExpireTime());
        return userInfoDataVO;
    }

    public static List<UserInfoDataVO> entityListToVoList(List<UserInfoDataEntity> userInfoDataEntityList) {
        if (CollectionUtils.isEmpty(userInfoDataEntityList)) {
            return Lists.newArrayList();
        }
        List<UserInfoDataVO> userInfoDataVOList = Lists.newArrayList();
        for (UserInfoDataEntity userInfoDataEntity : userInfoDataEntityList) {
            userInfoDataVOList.add(userInfoDataEntityToUserInfoDataVO(userInfoDataEntity));
        }
        return userInfoDataVOList;
    }
}
