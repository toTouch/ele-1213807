package com.xiliulou.electricity.utils;


import com.xiliulou.electricity.exception.BizException;

import java.util.Objects;

/**
 * <p>
 * Description: This class is VersionRange!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 * <p> 例子 :
 * <blockquote><pre>
 *  VersionRange range = new VersionRange("[2.1.1,2.2.1]") // 闭包区间
 *  Boolean isRange = range.isWithinRange(new Version("2.1.2"))
 *  // 包含版本 2.1.1~2.2.1
 *  </pre></blockquote>
 * <blockquote><pre>
 *  VersionRange range = new VersionRange("[2.1.1,2.2.1)") // 闭开区间
 *  Boolean isRange = range.isWithinRange(new Version("2.2.0"))
 *  // 包含版本 2.1.1~2.2.0
 *  </pre></blockquote>
 * <blockquote><pre>
 *  VersionRange range = new VersionRange("(2.1.1,2.2.1)") // 开区间
 *  Boolean isRange = range.isWithinRange(new Version("2.2.0"))
 *  // 包含版本 2.1.2~2.2.0
 *  </pre></blockquote>
 * </p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/10/21
 **/
public class VersionRange {
    
    private final Version lowerBound;
    
    private final Version upperBound;
    
    private final boolean lowerInclusive;
    
    private final boolean upperInclusive;
    
    public VersionRange(String range) {
        if (Objects.isNull(range)) {
            throw new BizException("Invalid range: null");
        }
        String cleanedRange = range.trim().replace(" ", "");
        lowerInclusive = cleanedRange.startsWith("[");
        upperInclusive = cleanedRange.endsWith("]");
        
        String[] bounds = cleanedRange.substring(1, cleanedRange.length() - 1).split(",");
        lowerBound = bounds[0].isEmpty() ? null : new Version(bounds[0]);
        upperBound = bounds[1].isEmpty() ? null : new Version(bounds[1]);
    }
    
    public boolean isWithinRange(Version version) {
        boolean lowerCheck = lowerBound == null || (lowerInclusive ? version.compareTo(lowerBound) >= 0 : version.compareTo(lowerBound) > 0);
        
        boolean upperCheck = upperBound == null || (upperInclusive ? version.compareTo(upperBound) <= 0 : version.compareTo(upperBound) < 0);
        
        return lowerCheck && upperCheck;
    }
}
