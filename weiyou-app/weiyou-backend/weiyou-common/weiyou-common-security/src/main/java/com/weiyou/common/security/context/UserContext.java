package com.weiyou.common.security.context;

import com.weiyou.common.core.exception.BusinessException;

public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static LoginUser require() {
        LoginUser loginUser = HOLDER.get();
        if (loginUser == null) {
            throw new BusinessException(401, "unauthorized");
        }
        return loginUser;
    }

    public static Long requireUserId() {
        return require().userId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
