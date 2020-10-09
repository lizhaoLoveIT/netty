package cn.lizhaoloveit.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;

/**
 * DESCRIPTION: 自定义服务端处理器
 * 需求: 用户提交一个请求后，在浏览器上就会看到 hello netty world
 * Author: ammar
 * Date:   2020-09-25
 * Time:   14:14
 */
public class AMServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 当 Channel 中有来自于客户端的数据时就会触发该方法的执行
     * @param ctx 上下文对象
     * @param msg 来自于 Channel 中的数据，也就是客户端的数据
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String text = ((TextWebSocketFrame) msg).text();
        ctx.channel().writeAndFlush(new TextWebSocketFrame("From Client: " + text));
    }

    /**
     * 当 Channel 中的数据在处理过程中出现异常时会触发该方法的执行
     * @param ctx 上下文
     * @param cause 发生的异常对象
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        // 关闭 channel 后会触发 closeFuture
        ctx.close();
    }
}
