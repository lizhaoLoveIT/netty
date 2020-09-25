package cn.lizhaoloveit.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * DESCRIPTION:
 * Author: ammar
 * Date:   2020-09-25
 * Time:   17:57
 */
public class AMClientHandler extends ChannelInboundHandlerAdapter {

    String message = "hello word";

    /**
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        for (int i = 0; i < 200; i++) {
//            ctx.writeAndFlush(message);
//        }
        byte[] bytes = message.getBytes();
        ByteBuf buf = null;
        for (int i = 0; i < 200; i++) {
            buf = Unpooled.buffer(bytes.length);
            buf.writeBytes(bytes);
            ctx.writeAndFlush(buf);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
