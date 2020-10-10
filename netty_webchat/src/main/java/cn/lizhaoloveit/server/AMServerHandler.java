package cn.lizhaoloveit.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * DESCRIPTION:
 * Author: ammar
 * Date:   2020-09-25
 * Time:   16:57
 */
public class AMServerHandler extends ChannelInboundHandlerAdapter {

    private AMHttpService httpService;
    private AMWebSocketService webSocketService;

    // 创建一个 ChannelGroup
    // thread-safe Set 提供了各种批量操作
    // 存放着与当前服务器相连接的所有 Active 状态的 Channel
    // GlobalEventExecutor 是一个单粒、单线程的 EventExecutor，
    // 为了保证对当前 Group 中的所有 Channel 的处理线程是同一个线程
    private static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public AMServerHandler(AMHttpService httpService, AMWebSocketService webSocketService) {
        this.httpService = httpService;
        this.webSocketService = webSocketService;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        group.writeAndFlush(new TextWebSocketFrame(ctx.channel().remoteAddress() + "上线了"));
        group.add(ctx.channel());
    }

    // 只要有客户端 Channel 给当前的服务端发送了消息，那么就会触发该方法的执行
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            httpService.handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            webSocketService.handleFrame(ctx, (WebSocketFrame) msg, group);
        }
    }

    // 只要有客户端 channel 断开与服务端的连接就会执行这个方法
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        // group 中存放的都是 Active 状态的 channel，一旦某个 channel 的状态不再是 Active
        // group 会自动将其从集合中踢出，所以下面的语句不用写。
        // remove() 方法的应用场景是，讲一个 Active 状态的 channel 移除 group 时使用。
        // group.remove(channel);
        group.writeAndFlush(new TextWebSocketFrame(channel.remoteAddress() + "下线了"));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
