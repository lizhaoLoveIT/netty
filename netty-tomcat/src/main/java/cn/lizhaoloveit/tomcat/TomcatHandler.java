package cn.lizhaoloveit.tomcat;

import cn.lizhaoloveit.servnet.Servnet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DESCRIPTION:
 *
 * 1. 从用户请求的 URI 中解析出要访问的 Servnet 名称
 * 2. 从 nameToServnetMap 中查找是否存在该名称的 Key，若存在则直接使用该实例，否则执行第三步
 * 3. 从 nameToClassnameMap 中查找是否存在该名称的 Key，若存在则获取到其对应的全限定类名
 * 4. 使用反射机制创建相应的 Servnet 实例，并写入到 nameToServnetMap 中，若不存在则直接访问默认 Servnet
 *
 * Author: ammar
 * Date:   2020-10-11
 * Time:   14:16
 */
public class TomcatHandler extends ChannelInboundHandlerAdapter {

    private Map<String, String> nameToClassNameMap;
    private Map<String, Servnet> nameToServnetMap;

    public TomcatHandler(Map<String, String> nameToClassNameMap, Map<String, Servnet> nameToServnetMap) {
        this.nameToClassNameMap = nameToClassNameMap;
        this.nameToServnetMap = nameToServnetMap;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            // 从 URI 解析出 Servnet 名称
            String servnetName = request.uri().split("/")[1];
            Servnet servnet = null;
            if (nameToServnetMap.containsKey(servnetName)) {
                servnet = nameToServnetMap.get(servnetName);
            } else if (nameToClassNameMap.containsKey(servnetName)) {
                // double-check 双重检测锁
                if (nameToServnetMap.get(servnetName) == null) {
                    synchronized (this) {
                        if (nameToServnetMap.get(servnetName) == null) {
                            // 创建 Servnet 实例，存入集合中
                            String className = nameToClassNameMap.get(servnetName);
                            servnet = (Servnet) Class.forName(className).newInstance();
                            nameToServnetMap.put(servnetName, servnet);
                        }
                    }
                }
            } else {
                servnet = new DefaultServnet();
            }
            // Servnet 不为空
            DefaultNettyRequest nettyRequest = new DefaultNettyRequest(request);
            DefaultNettyResponse nettyResponse = new DefaultNettyResponse(request, ctx);
            if (request.method().name().equalsIgnoreCase("GET")) {
                servnet.doGet(nettyRequest, nettyResponse);
            } else if (request.method().name().equalsIgnoreCase("POST")) {
                servnet.doPost(nettyRequest, nettyResponse);
            }
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
