package com.xiliulou.electricity.vo.userinfo.userInfoGroup;

import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupNamesBO;
import lombok.Data;

import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/10/10 11:38
 */
@Data
public class UserInfoGroupForUserVO {
    
    private Long franchiseeId;
    
    private String franchiseeName;
    
    List<UserInfoGroupNamesBO> userInfoGroupNames;
}
