package cn.lizhaoloveit.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * DESCRIPTION:
 * Author: ammar
 * Date:   2020-10-10
 * Time:   05:56
 */
public interface AMHttpService {
    void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request);
}
