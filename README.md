[spring cloud alibaba对应boot、cloud版本](https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E#%E7%BB%84%E4%BB%B6%E7%89%88%E6%9C%AC%E5%85%B3%E7%B3%BB)<br />[Spring Cloud Gateway微服务网关教程](https://zhuanlan.zhihu.com/p/327686172)<br />[Spring Cloud Gateway-官网介绍](https://cloud.spring.io/spring-cloud-gateway/reference/html/#the-path-route-predicate-factory)

- [x] spring cloud gateway实现API发布、路由转发
- [x] 引入sentinel实现限流和熔断；引入sentinel-dashboard实现限流监控
- [x] 考虑接口安全设计：密文、防重放、签名和验签
- [x] 全局异常处理
- [x] 日志配置
- [x] 自动部署
- [x] actuator健康检查
- [ ] 打印日志？打哪些？请求和响应内容打不打？
- [x] 占用哪些端口？sentinel-dashboard和sentinel-client
- [x] sentinel-dashboard部署在哪台机器？

# 坑指南
## 无法添加自定义API分组
### 现象：
经过反复验证测试，spring cloud gateway+sentinel搭建微服务网关时，硬编码配置限流规则时，无法配置自定义API分组`Set<ApiDefinition> `加载到`GatewayApiDefinitionManager`中。如果有相关硬编码，则启动时无法被sentinel dashboard识别，**但限流规则仍然生效**。而zuul+sentinel搭建微服务网关，硬编码配置限流规则可以配置自定义API分组。
### 原因：
尚不明确，可能是sentinel的bug。
### 测试过程：
1. 硬编码限流规则，添加自定义API分组，启动sentinel dashboard，分别启动zuul和gateway，发现gateway不被识别，zuul被识别；2. 硬编码限流规则，去掉自定义API分组，启动sentinel dashboard，分别启动zuul和gateway，两者均被识别。3. 继续测试其他限流规则硬编码的影响，例如普通限流规则、热点限流规则、熔断降级规则、系统规则，均不会造成sentinel dashboard不识别网关应用的现象。
### 解决办法：
**虽然sentinel+gateway无法通过代码硬编码配置自定义API分组，但gateway本身可以在路由配置中，将API分组为不同的路由，因此可以替代sentinel的自定义API分组。**后续如果进行规则持久化到DB的优化，有可能解决这一问题。
## sentinel指定目录失效
修改spring.cloud.sentinel.log.dir，不起作用，还是会到默认的~/logs/csp路径来。<br />经过测试，2.6.11不生效，2.6.3版本生效

| 2021.0.4.0* | Spring Cloud 2021.0.4 | 2.6.11 |
| --- | --- | --- |
| 2021.0.1.0 | Spring Cloud 2021.0.1 | 2.6.3 |

# 请求流重复获取
## 参考

自定义过滤器<br />[Spring Cloud Gateway---自定义过滤器 - 掘金](https://juejin.cn/post/6844903795973947400)<br />[Spring Cloud Gateway 4 自定义Filter - 路迢迢 - 博客园](https://www.cnblogs.com/chenglc/p/13139407.html)

请求流重复获取的一些实现，但可能有问题？<br />[Gateway网关自定义拦截器的不可重复读取数据_databufferutils.join_飘零未归人的博客-CSDN博客](https://blog.csdn.net/qq_34484062/article/details/122358110)<br />[SpringCloud Gateway自定义filter获取body中的数据为空_mameng1998的博客-CSDN博客](https://blog.csdn.net/mameng1988/article/details/109359745)<br />[SpringCloud Gateway读取Request Body方式 - 掘金](https://juejin.cn/post/7135067601977212942)<br />[Spring Cloud Gateway（读取、修改 Request Body） - 掘金](https://juejin.cn/post/7026589090317336613)

讲了几种请求流重复获取方案的缺陷：[Spring Cloud Gateway 之获取请求体（Request Body）的几种方式 - 码农的进击 - 博客园](https://www.cnblogs.com/hyf-huangyongfei/p/12849406.html)

请求流获取和修改的官网API：<br />[Spring Cloud Gateway](https://docs.spring.io/spring-cloud-gateway/docs/3.1.6/reference/html/#gatewayfilter-factories)<br />结合官网API的实现：<br />[ModifyRequestBodyGatewayFilterFactory获取并修改请求体_GavinYCF的博客-CSDN博客](https://blog.csdn.net/yucaifu1989/article/details/107531327)<br />[gateway 网关过滤器之修改body内容](https://www.jianshu.com/p/febb5898bf6a)

Spring cloud gateway中的request是基于Web Flux，和普通的请求方式不同，不能直接按照普通的`ServletRequest`去处理请求数据，而是一个`reactive.ServletRequest`。
## 代码
如果使用官网API的`ModifyRequestBodyGatewayFilterFactory`去修改请求数据，那么整个路由配置无法放在配置文件`application.yml`中，因为官网API需要一个`RewriteFunction`，在配置文件中只能写类名，类名是`String`，无法转换为`RewriteFunction`。官网API也声明了该过滤器只能通过Java代码实现。
### 代码实现
```java
package com.bocd.mkt.gatewaysentinelsample.config;

import com.bocd.mkt.gatewaysentinelsample.filter.SecurityRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

@Configuration
public class GatewayRoutesConfig {

    @Autowired
    private SecurityRequestFilter securityRequestFilter;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder){

        return builder.routes().route("test", r -> r
                        .path("/api-a/**")
                        .filters(f -> f
                                .modifyRequestBody(String.class,String.class, MediaType.APPLICATION_JSON_VALUE, securityRequestFilter))
                        .uri("lb://RULE-LITEFLOW")
        ).build();
    }
}
```
### 配置信息
### ~~解决方式1~~
继承`ModifyRequestBodyGatewayFilterFactory`，重写其配置，将其配置信息改为字符串类型，然后在实现方法中根据`RewriteFunction`类名去反射调用spring容器中的`RewriteFunction`。

> 方式1可能有潜在问题，不然为啥gateway官方不这样做？

### ~~解决方式2~~
按照`ModifyRequestBodyGatewayFilterFactory`的实现方式，简化其配置，在原方法中，为了能兼容大多数情况，该类需要传入一个输入类名、输出类名、请求的content-type和一个重写方法`RewriteFunction`，实际上，我们不需要这么多参数和步骤。
### 解决方式3
按照官网API建议，将路由配置写在代码中。
# 全局异常处理
## 参考
全局异常处理<br />[Spring Cloud Gateway-自定义异常处理 - duanxz - 博客园](https://www.cnblogs.com/duanxz/p/14786337.html)<br />[【SpringCloud】Spring Cloud Gateway 网关全局异常处理_springcloudgateway全局异常处理_islin_7的博客-CSDN博客](https://blog.csdn.net/qq_45694687/article/details/115109165)<br />[优雅的自定义Spring Cloud Gateway全局异常处理](https://www.jianshu.com/p/45f60467565b)

sentinel的<br />[api-gateway-flow-control | Sentinel](https://sentinelguard.io/zh-cn/docs/api-gateway-flow-control.html)<br />[Gateway结合Sentinel1.8限流熔断管理以及自定义异常](https://zhuanlan.zhihu.com/p/272673691?utm_id=0)<br />[SpringCloud: gateway整合sentinel 自定义异常处理类_amadeus_liu2的博客-CSDN博客](https://blog.csdn.net/amadeus_liu2/article/details/129082049)<br />[sentinel接入网关 自定义异常](https://zhuanlan.zhihu.com/p/363941009)
# 端点应用监控
## 参考
[Spring Boot 2.x系列【20】应用监控篇之Actuator入门案例及端点配置详解_management.endpoints_云烟成雨TD的博客-CSDN博客](https://blog.csdn.net/qq_43437874/article/details/119671958)

# 性能测试
## 加密解密
[jmeter加密解密（加密篇）](https://www.shuzhiduo.com/A/obzbaRVydE/)
# 打印日志
[Spring Cloud Gateway, logging request/response](https://stackoverflow.com/questions/74962401/spring-cloud-gateway-logging-request-response)<br />[SpringCloudGateway - Log incoming request url and corresponding route URI](https://stackoverflow.com/questions/54117061/springcloudgateway-log-incoming-request-url-and-corresponding-route-uri)
# 责任链
[spring 设计模式中的责任链怎么实现？ - 知乎](https://www.zhihu.com/question/590317051)
