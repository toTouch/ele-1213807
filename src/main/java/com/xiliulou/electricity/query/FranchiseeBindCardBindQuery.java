package com.xiliulou.electricity.query;
import lombok.Data;

import java.util.List;

/**
 * 加盟商活动绑定表(TFranchiseeBindActivity)实体类
 *
 * @author makejava
 * @since 2021-04-17 13:32:44
 */
@Data
public class FranchiseeBindCardBindQuery {
    /**
    * 加盟商id
    */
    private Integer franchiseeId;
    /**
    * 活动id
    */
    private List<Integer> cardIdList;


}
