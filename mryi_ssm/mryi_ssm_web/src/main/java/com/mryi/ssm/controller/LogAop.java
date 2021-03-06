package com.mryi.ssm.controller;

import com.mryi.ssm.SysLog;
import com.mryi.ssm.service.SysLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;

@Component
@Aspect
public class LogAop {
  @Autowired SysLogService sysLogService;
  @Autowired HttpServletRequest request;
  private Date visitTime; // 开始访问的时间
  private Class clazz; // 访问的类
  private Method method; // 访问的方法
  // 前置通知  主要获取开始的时间 和执行的类是哪一个,执行的是哪一个方法
  @Before("execution(* com.mryi.ssm.controller.*.*(..))")
  public void doBefore(JoinPoint joinPoint) throws Exception {
    visitTime = new Date(); // 当前时间,就是开始访问的时间
    clazz = joinPoint.getTarget().getClass(); // 具体要访问的类
    String methodName = joinPoint.getSignature().getName(); // 获取访问的方法的名字
    Object[] args = joinPoint.getArgs();
    if (args == null || args.length == 0) {

      method = clazz.getMethod(methodName);

    } else {
      Class[] classArgs = new Class[args.length];
      for (int i = 0; i < args.length; i++) {
        classArgs[i] = args[i].getClass();
      }
      method = clazz.getMethod(methodName, classArgs);
    }
  }
  // 后置通知
  @After("execution(* com.mryi.ssm.controller.*.*(..))")
  public void doAfter(JoinPoint joinPoint) throws Exception {
    long time = new Date().getTime() - visitTime.getTime(); // 获取访问时长
    String url = "";
    // 获取url
    if (clazz != null && method != null & clazz != LogAop.class) {
      // 获取类上的@RequestMapping("/orders")
      RequestMapping classAnnotation = (RequestMapping) clazz.getAnnotation((RequestMapping.class));
      if (classAnnotation != null) {
        String[] classValue = classAnnotation.value();
        // 获取方法上的@RequestMapping(xx)
        RequestMapping methodAnnotation = method.getAnnotation(RequestMapping.class);
        if (methodAnnotation != null) {
          String[] methodValue = methodAnnotation.value();
          url = classValue[0] + methodValue[0];
        }
      }
    }
    // 获取ip
    String ip = request.getRemoteAddr();
    // 获取当前操作用户
    SecurityContext context = SecurityContextHolder.getContext(); // 通过上下文中回去当前登陆的额用户
    User user = (User) context.getAuthentication().getPrincipal();
    String username = user.getUsername(); // 拿到操作用户的用户名
    // 将日志相关信息封装到SysLog对象中
    SysLog sysLog = new SysLog();
    sysLog.setExecutionTime(time);
    sysLog.setIp(ip);
    sysLog.setMethod("[类名]" + clazz.getName() + "[方法名]" + method.getName());
    sysLog.setUrl(url);
    sysLog.setUsername(username);
    sysLog.setVisitTime(visitTime);
    // 调用service
    sysLogService.save(sysLog);
  }
}
