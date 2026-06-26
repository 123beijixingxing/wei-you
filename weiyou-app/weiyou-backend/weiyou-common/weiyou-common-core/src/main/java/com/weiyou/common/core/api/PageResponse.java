package com.weiyou.common.core.api;

import java.util.List;

public record PageResponse<T>(
        List<T> list,
        int pageNo,
        int pageSize,
        long total,
        boolean hasMore,
        String nextCursor
) {

    public static <T> PageResponse<T> of(List<T> list, int pageNo, int pageSize, long total, boolean hasMore, String nextCursor) {
        return new PageResponse<>(list, pageNo, pageSize, total, hasMore, nextCursor);
    }
}
