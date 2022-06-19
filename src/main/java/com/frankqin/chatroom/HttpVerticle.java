package com.frankqin.chatroom;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import java.util.UUID;

/**
 * @title: HttpVerticle
 * @Author: Frank Qin
 * @Description:
 * @Date: 2022/6/20 00:04
 * @Version 1.0
 */
public class HttpVerticle extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new HttpVerticle());
    }

    @Override
    public void start() throws Exception {

        int port = config().getInteger("port", 8888);

        System.out.println("Hello Vert.x");

        // 创建桥，基于消息的通讯
        EventBus eb = vertx.eventBus();

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.route("/hello").handler(ctx -> {

            // This handler will be called for every request
            HttpServerResponse response = ctx.response();
            response.putHeader("content-type", "text/plain");

            // Write to the response and end it
            response.end("Hello World from Vert.x-Web!");
        });

        PermittedOptions inboundPermitted1 = new PermittedOptions()
                .setAddress("chatroom");

        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        SockJSBridgeOptions options = new SockJSBridgeOptions()
                .addInboundPermitted(inboundPermitted1)
                .addOutboundPermitted(inboundPermitted1)
                ;
        // mount the bridge on the router
        router
                .route("/eventbus/*")
                .subRouter(sockJSHandler.bridge(options));


        // 从resources/webroot中调用html模版
        router.route().handler(StaticHandler.create());

        server.requestHandler(router).listen(port, serverAsyncResult -> {
            if (serverAsyncResult.succeeded()) {
                System.out.println("启动在" + serverAsyncResult.result().actualPort() + "端口");
            }
        });

        // 间隔1s测试输出
//        vertx.setPeriodic(1000, now->{
//            eb.publish("chatroom",
//                    new JsonObject()
//                            .put("name", "vertx")
//                            .put("content", UUID.randomUUID().toString())
//            );
//        });
    }
}
