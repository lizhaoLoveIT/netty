package cn.lizhaoloveit.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * DESCRIPTION:
 * Author: ammar
 * Date:   2020-09-25
 * Time:   04:15
 */
public class AMServer {
    public static void main(String[] args) throws InterruptedException {

        // 处理客户端连接请求，请求成功后将请求发送给 childGroup 中的 eventLoop
        NioEventLoopGroup parentGroup = new NioEventLoopGroup();
        // 处理客户端请求
        NioEventLoopGroup childGroup = new NioEventLoopGroup();

        // 启动 ServerChannel
        ServerBootstrap bootstrap = new ServerBootstrap();
        // 指定 eventLoopGroup
        bootstrap.group(parentGroup, childGroup)
                // 指定使用 NIO(异步) 进行通信
                .channel(NioServerSocketChannel.class)
                // parentGroup 中 eventLoop 绑定的线程的处理器
//                .handler()
                // 指定 childGroup 中 eventLoop 绑定的线程的处理请求的处理器
                .childHandler(new AMChannelInitializer())
        ;
        ChannelFuture channelFuture = null;
        try {
            // 绑定端口号
            // bind() 方法的执行时异步的
            // .sync() 使 bind() 操作与后续的代码的执行由异步变为了同步
            channelFuture = bootstrap.bind(8888).sync();
            System.out.println("服务器启动成功。监听端口号为：8888");
            // 关闭 Channel closeFuture() 的执行时异步的
            // 当 Channel 调用了 close() 方法并关闭成功后才会触发 closeFuture() 方法的执行
            // closeFuture() 返回 ChannelFuture，说明该方法为异步
            // 所以此处 .sync() 确保 closeFuture() 执行完毕后才继续执行下去
            channelFuture.channel().closeFuture().sync();
        }  finally {
            // 优雅关闭，执行完的才会关闭。
            parentGroup.shutdownGracefully(); // 关闭线程
            childGroup.shutdownGracefully();
        }
    }
}
