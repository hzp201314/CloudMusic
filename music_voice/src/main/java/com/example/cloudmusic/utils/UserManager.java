package com.example.cloudmusic.utils;

import com.example.cloudmusic.model.user.User;

/**
 * 单例管理登陆用户信息
 */
public class UserManager {
    private static UserManager userManager = null;
    private User user = null;

    //双重锁单例
    public static UserManager getInstance() {
        if (userManager == null) {
            synchronized (UserManager.class) {
                if (userManager == null) {
                    userManager = new UserManager();
                }
                return userManager;
            }
        } else {
            return userManager;
        }
    }

    /**
     * 获取用户信息
     * @return 用户信息
     */
    public User getUser() {
        return user;
    }

    /**
     * 设置保存用户信息
     * @param user 用户信息
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * 清除登陆信息
     */
    public void removeUser() {
        this.user = null;
    }

    /**
     * 是否登陆
     * @return 是否登陆
     */
    public boolean hasLogined() {
        return user != null;
    }
}
