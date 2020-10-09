package cn.lizhaoloveit.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * DESCRIPTION:
 * Author: ammar
 * Date:   2020-09-25
 * Time:   04:15
 */
public class AMServer {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup parentGroup = new NioEventLoopGroup();
        NioEventLoopGroup childGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(parentGroup, childGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        // 添加 http 编解码器
                        pipeline.addLast(new HttpServerCodec());
                        // 添加大块数据 Chunk 处理器
                        /**
                         *  A {@link ChannelHandler} that adds support for writing a large data stream
                         *  asynchronously neither spending a lot of memory nor getting
                         *  {@link OutOfMemoryError}
                         *  用来支持写入大的数据流，不会抛内存溢出的错误，例如文件传输
                         *  这种 chunk 在传输的时候不会做二进制编码，需要接收方自己去解释，而且需要接收方把自己所有的
                         *  chunk 自行组装并解码
                         */
                        pipeline.addLast(new ChunkedWriteHandler());
                        // 添加 Chunk 聚合处理器
                        // 将收集到的 chunk 聚合成 HTTP 请求。
                        // 如果长度超出 4096 handleOversizedMessage 异常会抛出
                        pipeline.addLast(new HttpObjectAggregator(4096));
                        // 添加 webSocket 协议转换处理器，将 HTTP 请求转换成 webSocket
                        pipeline.addLast(new WebSocketServerProtocolHandler("/some"));
                        // 添加自定义处理器
                        pipeline.addLast(new AMServerHandler());
                        // 处理器顺序不能颠倒
                    }
                })
        ;
        ChannelFuture channelFuture = null;
        try {
            channelFuture = bootstrap.bind(8888).sync();
            System.out.println("服务器启动成功。监听端口号为：8888");
            channelFuture.channel().closeFuture().sync();
        }  finally {
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
        }
    }
}
