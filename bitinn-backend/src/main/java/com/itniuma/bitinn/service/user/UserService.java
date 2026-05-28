package com.itniuma.bitinn.service.user;

import com.itniuma.bitinn.pojo.vo.Result;
import com.itniuma.bitinn.pojo.entity.User;

/**
 * 用户服务接口
 * 定义用户注册、登录、信息查询与修改、密码更新的操作契约。
 *
 * @author aceFelix
 */
public interface UserService {

    /**
     * 用户注册，支持幂等性校验和分布式锁防重复提交
     */
    Result register(String username, String password, String email);

    /**
     * 用户登录，支持失败次数限制，返回 JWT Token
     */
    Result<String> login(String username, String password);

    /**
     * 从 ThreadLocal 获取当前登录用户信息
     */
    Result<User> getUserInfo();

    /**
     * 更新用户昵称等信息，同步 ES 冗余字段
     */
    Result updateUserInfo(User user);

    /**
     * 更新用户头像
     */
    Result updateAvatar(String avatarUrl);

    /**
     * 修改密码，校验原密码后更新，并将当前 Token 加入黑名单
     */
    Result updatePassword(String oldPwd, String newPwd, String rePwd);

    /**
     * 根据 ID 获取用户信息
     */
    Result<User> getUserById(Integer id);
}
