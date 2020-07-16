package com.mycompany.app;

import java.net.InetSocketAddress;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;

import reactor.netty.tcp.ProxyProvider;

public class App {
    public static void main(String[] args) throws Exception {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");

        String url = "https://www.example.org";

        reactor.netty.http.client.HttpClient nettyClient = reactor.netty.http.client.HttpClient.create()
                .tcpConfiguration(tcpClient -> tcpClient
                        .proxy(opt -> opt.type(ProxyProvider.Proxy.HTTP).host("localhost").port(8888)));

        // Sleep between requests to prevent creating new TCP connection
        test(nettyClient, url);
        Thread.sleep(1000);
        test(nettyClient, url);
        Thread.sleep(1000);
        test(nettyClient, url);


        HttpClient azureClient = new NettyAsyncHttpClientBuilder()
            .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)))
            .build();

        // Sleep between requests to prevent creating new TCP connection
        test(azureClient, url);
        Thread.sleep(1000);
        test(azureClient, url);
        Thread.sleep(1000);
        test(azureClient, url);
    }

    private static void test(reactor.netty.http.client.HttpClient nettyClient, String url) {
        String response = nettyClient.get().uri(url).responseContent().aggregate().asString().block();
        System.out.println(response.length());
    }

    private static void test(HttpClient azureClient, String url) {
        HttpResponse response = azureClient.send(new HttpRequest(HttpMethod.GET, url)).block();
        System.out.println(response.getBodyAsByteArray().block().length);
    }

}
