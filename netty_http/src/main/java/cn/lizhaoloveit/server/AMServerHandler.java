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
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            System.out.println("请求方式:" + request.method().name());
            System.out.println("请求URI:" + request.uri());

            if ("/favicon.ico".equals(request.uri())) {
                // 不处理 favicon.ico 请求
                return;
            }

            // 构造响应体
            ByteBuf body = Unpooled.copiedBuffer("hello netty word", CharsetUtil.UTF_8);

            // 响应客户端，fullHttpResponse 要求既有响应体又要有响应头
            // 生成响应对象
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, body);
            // 获取到 response 的 header 初始化
            HttpHeaders headers = response.headers();
            headers.set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            // readableBytes 可读的字节，buffer 没填满，只获取可读的长度
            headers.set(HttpHeaderNames.CONTENT_LENGTH, body.readableBytes());

            // 将响应对象写入 channel
            // ctx.write(response);
            // ctx.flush();
            ctx.writeAndFlush(response)
                    // 添加监听器，响应体发送完毕则直接关闭 channel
                    .addListener(ChannelFutureListener.CLOSE);
        }
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
