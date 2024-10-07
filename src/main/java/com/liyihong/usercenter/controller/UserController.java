package com.liyihong.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liyihong.usercenter.common.BaseResponse;
import com.liyihong.usercenter.common.ErrorCode;
import com.liyihong.usercenter.common.ResultUtils;
import com.liyihong.usercenter.contant.UserConstant;
import com.liyihong.usercenter.exception.BusinessException;
import com.liyihong.usercenter.service.UserService;
import com.liyihong.usercenter.model.domain.User;
import com.liyihong.usercenter.model.domain.request.UserLoginRequest;
import com.liyihong.usercenter.model.domain.request.UserRegisterRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户接口
 *
 * @author liyihong
 */
@RestController//返回值的类型是一个json类型，适用于编写result风格的api
@RequestMapping("/user")
public class UserController {

    @Resource//spring提供的注解，可以把相关的类引入到该类中。
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    //@RequestBody该标签用于将前端传来的json参数和该对象进行关联
    //BaseResponse 将返回的类型统一用该类进行封装，返回给前端对应的状态信息。
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {//将参数直接封装为了一个参数的类在request包中
        // 校验
        if (userRegisterRequest == null) {//传来的参数不能为空，否则就请求业务逻辑层来实现注册
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //拿到参数的比较标准的写法
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        //对拿到的参数进行判断，任何一个都不能为空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);//将返回的对象封装为ResultUtils单独的一个类。
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    //@RequestBody可以让spring框架知道让前端的json参数去关联
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户
     *
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        // TODO 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }


    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "缺少管理员权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();//QueryWrapper是mybatis-plus的一个条件构造器
        if (StringUtils.isNotBlank(username)) {//不为空
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        //把userList转换为一个数据流，然后遍历每个元素，把每个元素的密码设置为空，再拼接为一个list去返回
        //getSafetyUser算是一个优化，防止代码的复用，定义在UserServer和UserServerImpl的实现类中
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {//前端传来的id小于等于0就不符合规范
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }


    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == UserConstant.ADMIN_ROLE;//这些常量直接封装在了contant包下
    }

}
