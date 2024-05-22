package com.xiliulou.electricity.enums.supper;


import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: This enum is GrantType!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/5/21
 **/
public enum GrantType {
    OPERATE_USER(0, "OPERATE_USER"),
    FRANCHISEE_USER(1, "FRANCHISEE_USER"),
    STORE_USER(2, "STORE_USER"),
    ALL(-1, "全部");
    
    private final Integer code;
    
    @Getter
    private final String name;
    
    GrantType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public static Set<String> namesOfCode(List<Integer> code) {
        if (CollectionUtils.isEmpty(code) || code.contains(-1)){
            return Arrays.stream(GrantType.values()).filter(g -> !g.code.equals(-1)).map(GrantType::getName).collect(Collectors.toSet());
        }
        return Arrays.stream(GrantType.values()).filter(g -> code.contains(g.code)).map(GrantType::getName).collect(Collectors.toSet());
    }
}
