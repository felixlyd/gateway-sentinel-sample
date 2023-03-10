package com.bocd.mkt.gatewaysentinelsample.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * sentinel网关规则限流配置
 *
 * @author : liuyaodong
 * @date 2023/2/24
 */
@Configuration
@Slf4j
public class SentinelRuleConfig implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("--------------加载自定义API-------------");
//        initCustomizedApis();
        log.info("--------------加载自定义规则-------------");
        initGatewayRules();
        initDegradeRules();
        initSystemRule();
    }
    private void initCustomizedApis() {
        Set<ApiDefinition> definitions = new HashSet<>();
        ApiDefinition api1 = new ApiDefinition("rule_api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/api-a/rule"));
                    add(new ApiPathPredicateItem().setPattern("/api-a/refresh"));
                }});
        definitions.add(api1);
        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
    }

    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        rules.add(new GatewayFlowRule("rule_api")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setCount(10)
                .setIntervalSec(1)
        );

        rules.add(new GatewayFlowRule("test")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_ROUTE_ID)
                .setCount(10)
                .setIntervalSec(1)
        );
        GatewayRuleManager.loadRules(rules);
    }

    private void initDegradeRules(){
        List<DegradeRule> rules = new ArrayList<>();
        rules.add(new DegradeRule("rule_api")
                .setCount(5)
                .setGrade(CircuitBreakerStrategy.ERROR_COUNT.getType())
                .setTimeWindow(10)
        );
        DegradeRuleManager.loadRules(rules);
    }

    private void initSystemRule() {

        List<SystemRule> rules = new ArrayList<>();

        SystemRule rule1 = new SystemRule();
        // max load is 3
        rule1.setHighestSystemLoad(3.0);
        // max cpu usage is 60%
        rule1.setHighestCpuUsage(0.6);
        // max avg rt of all request is 10 ms
        rule1.setAvgRt(10);
        // max total qps is 20
        rule1.setQps(20);
        // max parallel working thread is 10
        rule1.setMaxThread(10);

        rules.add(rule1);

        SystemRuleManager.loadRules(rules);
    }
}
