package com.bark.conf;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;import org.springframework.web.client.RestTemplate;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


/**
 * @author ballen
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public CloseableHttpClient closeableHttpClient() {
        PoolingHttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                //设置线程池的最大线程数
                .setMaxConnTotal(180)
                .setMaxConnPerRoute(100)
                .build();
        return HttpClients.custom().setConnectionManager(cm).build();
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory(CloseableHttpClient closeableHttpClient) {
        return new HttpComponentsClientHttpRequestFactory(closeableHttpClient);
    }

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory clientHttpRequestFactory) {
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
        messageConverters.add(new FormHttpMessageConverter());
        messageConverters.add(new ByteArrayHttpMessageConverter());

        JacksonJsonHttpMessageConverter jacksonHttpMessageConverter = new JacksonJsonHttpMessageConverter(
                JsonMapper.builder()
                        // 兼容上游返回的含未转义控制字符的JSON
                        .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
                        .build());
        messageConverters.add(jacksonHttpMessageConverter);

        RestTemplate template = new RestTemplate(messageConverters);
        template.setRequestFactory(clientHttpRequestFactory);

        return template;
    }
}
