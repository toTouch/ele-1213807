package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Region;
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

    List<Franchisee> franchiseeList;

    Set<Region> regionList;
}
