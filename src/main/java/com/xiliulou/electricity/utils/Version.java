package com.xiliulou.electricity.utils;


import com.xiliulou.electricity.exception.BizException;

import java.util.Arrays;
import java.util.Objects;

/**
 * <p>
 * Description: This class is Version!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/10/21
 **/
public class Version implements Comparable<Version> {
    
    private final int[] parts;
    
    public Version(String version) {
        if (Objects.isNull(version)) {
            throw new BizException("Invalid version: null");
        }
        parts = Arrays.stream(version.split("\\.")).mapToInt(Integer::parseInt).toArray();
    }
    
    @Override
    public int compareTo(Version other) {
        int length = Math.max(this.parts.length, other.parts.length);
        for (int i = 0; i < length; i++) {
            int thisPart = i < this.parts.length ? this.parts[i] : 0;
            int otherPart = i < other.parts.length ? other.parts[i] : 0;
            if (thisPart != otherPart) {
                return Integer.compare(thisPart, otherPart);
            }
        }
        return 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Version version = (Version) o;
        return Arrays.equals(parts, version.parts);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(parts);
    }
    
    @Override
    public String toString() {
        return Arrays.toString(parts);
    }
}
