package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.entity.Franchisee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-12-15:27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FranchiseeAreaVO {
    /**
     * 1.区域内无加盟商；2.加盟商与区域完全匹配;3.区域内有多个加盟商；4.城市无加盟商；5.城市有一个匹配的加盟商；6.城市有多个加盟商,且都设置了区；7.城市有多个加盟商,且都没设置了区
     */
    private Integer result;

    List<Franchisee> franchiseeList;

    Set<String> regionList;

    public static final Integer NOT_FRANCHISEE= 1;
    public static final Integer ONE_FRANCHISEE= 2;
    public static final Integer MULTIPLE_FRANCHISEE= 3;
    public static final Integer NOT_FRANCHISEE_CITY= 4;
    public static final Integer ONE_FRANCHISEE_CITY= 5;
    public static final Integer MULTIPLE_FRANCHISEE_CITY_HAVE_REGION= 6;
    public static final Integer MULTIPLE_FRANCHISEE_CITY_NOT_REGION= 7;
}
