package com.adam.apidoc_center.datasource;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 读写分离切面配置
 */
@Aspect
@Component
@Slf4j
public class DynamicDataSourceAspect {

    @Pointcut("execution(public * com.adam.apidoc_center.repository..*.save*(..))")
    public void savePointcut() {
    }
    @Pointcut("execution(public * com.adam.apidoc_center.repository..*.delete*(..))")
    public void deletePointcut(){
    }
    @Pointcut("execution(public * com.adam.apidoc_center.repository..*.*(..))")
    public void anyPointcut(){
    }

    @Before("savePointcut() || deletePointcut()")
    public void masterMethods(JoinPoint joinPoint) {
        log.debug(joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName() + ",using master");
        DynamicDataSourceKeyHolder.useMaster();
    }

    @Before("anyPointcut() && !savePointcut() && !deletePointcut()")
    public void anyMethods(JoinPoint joinPoint) {
        log.debug(joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName() + ",using slave");
        DynamicDataSourceKeyHolder.useSlave();
    }

}