package cn.lizhaoloveit.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * DESCRIPTION:
 * Author: ammar
 * Date:   2020-10-10
 * Time:   05:57
 */
public interface AMWebSocketService {
    void handleFrame(ChannelHandlerContext ctx, WebSocketFrame frame, ChannelGroup group);
}
