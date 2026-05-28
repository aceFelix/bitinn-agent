package com.itniuma.bitinn.controller.user;

import com.itniuma.bitinn.pojo.vo.Result;
import com.itniuma.bitinn.pojo.entity.User;
import com.itniuma.bitinn.service.user.UserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * 管理用户注册、登录、信息查询和修改。
 * 使用 JSR-303 注解进行参数校验（@Pattern、@Email、@NotBlank）。
 *
 * 认证机制：
 *   登录成功返回 JWT Token，后续请求通过 Authorization Header 携带。
 *   用户身份通过 JwtAuthenticationFilter 解析后存入 ThreadLocalUtil。
 *
 * @author aceFelix
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("api/user")
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     *
     * @param username 用户名（3-16 位非空字符）
     * @param password 密码（6-20 位非空字符）
     * @param email    邮箱
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result register(
            @Pattern(regexp = "^\\S{3,16}$", message = "用户名必须是3-16位非空字符") @NotBlank String username,
            @Pattern(regexp = "^\\S{6,20}$", message = "密码必须是6-20位非空字符") @NotBlank String password,
            @Email(message = "邮箱格式不正确") @NotBlank String email
    ) {
        return userService.register(username, password, email);
    }

    /**
     * 用户登录
     *
     * 使用 BCrypt 验证密码，成功返回 JWT Token。
     *
     * @param username 用户名
     * @param password 密码
     * @return JWT Token 字符串
     */
    @PostMapping("/login")
    public Result<String> login(
            @Pattern(regexp = "^\\S{3,16}$", message = "用户名必须是3-16位非空字符") @NotBlank String username,
            @Pattern(regexp = "^\\S{6,20}$", message = "密码必须是6-20位非空字符") @NotBlank String password
    ) {
        return userService.login(username, password);
    }

    /**
     * 获取当前登录用户信息
     *
     * 从 ThreadLocalUtil 中获取 JWT 解析出的用户 ID，查询完整用户数据
     */
    @GetMapping("/info")
    public Result<User> getUserInfo() {
        return userService.getUserInfo();
    }

    /**
     * 更新用户信息（昵称、简介等）
     *
     * @param user 用户实体（仅更新非 null 字段）
     */
    @PutMapping("/update")
    public Result updateUserInfo(@RequestBody @Validated User user) {
        return userService.updateUserInfo(user);
    }

    /**
     * 更新头像
     *
     * @param avatarUrl 新头像 URL
     */
    @PatchMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam @NotBlank String avatarUrl) {
        return userService.updateAvatar(avatarUrl);
    }

    /**
     * 修改密码
     *
     * @param oldPwd 原密码
     * @param newPwd 新密码（6-20 位非空字符）
     * @param rePwd  确认新密码
     */
    @PatchMapping("/updatePwd")
    public Result updatePassword(
            @Pattern(regexp = "^\\S{6,20}$", message = "原密码格式不正确") @NotBlank String oldPwd,
            @Pattern(regexp = "^\\S{6,20}$", message = "新密码必须是6-20位非空字符") @NotBlank String newPwd,
            @Pattern(regexp = "^\\S{6,20}$", message = "确认密码格式不正确") @NotBlank String rePwd
    ) {
        return userService.updatePassword(oldPwd, newPwd, rePwd);
    }

    /**
     * 根据 ID 获取用户公开信息
     *
     * @param id 用户 ID
     * @return 用户公开信息（昵称、头像等）
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Integer id) {
        return userService.getUserById(id);
    }
}
