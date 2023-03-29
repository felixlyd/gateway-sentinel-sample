


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

<a name="npp6N"></a>
# 坑指南
<a name="hH7rT"></a>
## 无法添加自定义API分组
<a name="qrfGP"></a>
### 现象：
经过反复验证测试，spring cloud gateway+sentinel搭建微服务网关时，硬编码配置限流规则时，无法配置自定义API分组`Set<ApiDefinition> `加载到`GatewayApiDefinitionManager`中。如果有相关硬编码，则启动时无法被sentinel dashboard识别，**但限流规则仍然生效**。而zuul+sentinel搭建微服务网关，硬编码配置限流规则可以配置自定义API分组。
<a name="EIHSA"></a>
### 原因：
尚不明确，可能是sentinel的bug。
<a name="Rsj5e"></a>
### 测试过程：
1. 硬编码限流规则，添加自定义API分组，启动sentinel dashboard，分别启动zuul和gateway，发现gateway不被识别，zuul被识别；2. 硬编码限流规则，去掉自定义API分组，启动sentinel dashboard，分别启动zuul和gateway，两者均被识别。3. 继续测试其他限流规则硬编码的影响，例如普通限流规则、热点限流规则、熔断降级规则、系统规则，均不会造成sentinel dashboard不识别网关应用的现象。
<a name="QM2gM"></a>
### 解决办法：
**虽然sentinel+gateway无法通过代码硬编码配置自定义API分组，但gateway本身可以在路由配置中，将API分组为不同的路由，因此可以替代sentinel的自定义API分组。**后续如果进行规则持久化到DB的优化，有可能解决这一问题。
<a name="f28Jr"></a>
## sentinel指定目录失效
修改spring.cloud.sentinel.log.dir，不起作用，还是会到默认的~/logs/csp路径来。<br />经过测试，2.6.11不生效，2.6.3版本生效

| 2021.0.4.0* | Spring Cloud 2021.0.4 | 2.6.11 |
| --- | --- | --- |
| 2021.0.1.0 | Spring Cloud 2021.0.1 | 2.6.3 |

<a name="Y0QZQ"></a>
# 请求流重复获取
<a name="VjfpJ"></a>
## 参考

自定义过滤器



请求流重复获取的一些实现，但可能有问题？





讲了几种请求流重复获取方案的缺陷：

请求流获取和修改的官网API：

结合官网API的实现：



Spring cloud gateway中的request是基于Web Flux，和普通的请求方式不同，不能直接按照普通的`ServletRequest`去处理请求数据，而是一个`reactive.ServletRequest`。
<a name="kk9pW"></a>
## 代码
如果使用官网API的`ModifyRequestBodyGatewayFilterFactory`去修改请求数据，那么整个路由配置无法放在配置文件`application.yml`中，因为官网API需要一个`RewriteFunction`，在配置文件中只能写类名，类名是`String`，无法转换为`RewriteFunction`。官网API也声明了该过滤器只能通过Java代码实现。
<a name="VdG8p"></a>
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
<a name="WOZoD"></a>
### 配置信息
<a name="vE0mm"></a>
### ~~解决方式1~~
继承`ModifyRequestBodyGatewayFilterFactory`，重写其配置，将其配置信息改为字符串类型，然后在实现方法中根据`RewriteFunction`类名去反射调用spring容器中的`RewriteFunction`。

> 方式1可能有潜在问题，不然为啥gateway官方不这样做？

<a name="l7VeV"></a>
### ~~解决方式2~~
按照`ModifyRequestBodyGatewayFilterFactory`的实现方式，简化其配置，在原方法中，为了能兼容大多数情况，该类需要传入一个输入类名、输出类名、请求的content-type和一个重写方法`RewriteFunction`，实际上，我们不需要这么多参数和步骤。
<a name="Xs5SP"></a>
### 解决方式3
按照官网API建议，将路由配置写在代码中。
<a name="fkmWn"></a>
# 全局异常处理
<a name="OuShX"></a>
## 参考
全局异常处理




sentinel的




<a name="QyGrG"></a>
# 端点应用监控
<a name="RSYEF"></a>
## 参考


<a name="tME5l"></a>
# 性能测试
<a name="RHVCb"></a>
## 加密解密

<a name="aD6Tk"></a>
# 打印日志


<a name="DigLs"></a>
# 责任链

