package cn.lizhaoloveit.tomcat;

import cn.lizhaoloveit.servnet.Servnet;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

import javax.sound.midi.Soundbank;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DESCRIPTION:
 * Author: ammar
 * Date:   2020-10-11
 * Time:   12:40
 */
public class TomcatServer {
    // 会有很多客户端访问，是否存在线程安全问题，自定义的 servnet 类，因此不存在线程安全问题
    // key 为 servnet 简单类名，Value 为对应的 servnet 类的全限定名
    private Map<String, String> nameToClassNameMap = new HashMap<>();
    // 多个客户端会写入此 map，因此存在线程安全问题
    // key 为 servnet 简单类名，Value 为对应的 servnet 实例
    private Map<String, Servnet> nameToServnetMap = new ConcurrentHashMap<>();

    private String basePackage;

    public TomcatServer(String basePackage) {
        this.basePackage = basePackage;
    }

    // 启动 tomcat
    public void start() {
        // 加载指定包中的所有 servnet 类名
        cacheClassName(basePackage);
        // 启动 server 服务
        runServer();
    }

    private void cacheClassName(String basePackage) {
        String path = basePackage.replaceAll("\\.", "/");
        URL resource = this.getClass().getClassLoader()
                // cn.lizhaoloveit.webapp => cn/lizhaoloveit/webapp
                .getResource(path);
        // 若目录中没有任何资源，则直接结束
        if (resource == null) {
            System.out.println("==================");
            return;
        }

        File directory = new File(resource.getFile());
        // 查找所有 .class 文件
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                cacheClassName(basePackage + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String simpleClassName = file.getName().replace(".class", "").trim();
                nameToClassNameMap.put(simpleClassName.toLowerCase(), basePackage + "." + simpleClassName);
            }
        }
    }

    private void runServer() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            // 指定存放请求的队列长度，Socket 的标准参数
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            // 客户端一旦连上，服务端是否启用心跳机制来检测长连接的存活性，即客户端的存活性
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new HttpServerCodec());
                    pipeline.addLast(new TomcatHandler(nameToClassNameMap, nameToServnetMap));
                }
            });
            ChannelFuture future = bootstrap.bind(8888).sync();
            System.out.println("Tomcat 启动成功：监听端口号为8888");
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
