package cn.lizhaoloveit.tomcat;

import cn.lizhaoloveit.servnet.NettyResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.internal.StringUtil;

/**
 * DESCRIPTION:
 * Author: ammar
 * Date:   2020-10-11
 * Time:   12:21
 */
public class DefaultNettyResponse implements NettyResponse {
    private HttpRequest request;
    private ChannelHandlerContext context;

    public DefaultNettyResponse(HttpRequest request, ChannelHandlerContext context) {
        this.request = request;
        this.context = context;
    }

    @Override
    public void write(String content) throws Exception {
        // 处理 content  为空的情况
        if (StringUtil.isNullOrEmpty(content)) return;

        // 创建响应对象
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                // 根据响应体内容大小为 response 对象分配存储空间
                Unpooled.wrappedBuffer(content.getBytes("UTF-8")));

        // 获取响应头
        HttpHeaders headers = response.headers();
        headers.set(HttpHeaderNames.CONTENT_TYPE, "test/plain");
        headers.set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        headers.set(HttpHeaderNames.EXPIRES, 0); // 缓存不过期
        // 若 http 请求时长连接，则相应也使用长连接
        if (HttpUtil.isKeepAlive(request)) {
            headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        context.writeAndFlush(response); // 响应
    }
}
