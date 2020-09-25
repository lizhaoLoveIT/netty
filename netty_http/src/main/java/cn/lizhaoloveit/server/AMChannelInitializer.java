package cn.lizhaoloveit.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * DESCRIPTION:
 * Author: ammar
 * Date:   2020-09-25
 * Time:   13:56
 */
public class AMChannelInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * 当 Channel 初始化创建完毕后就会触发该方法，用于初始化 Channel
     * @param socketChannel
     * @throws Exception
     */
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        // 从 Channel 中获取 pipeline
        ChannelPipeline pipeline = socketChannel.pipeline();
        // 将 HttpServerCodec 处理器放入 pipline 的最后，先放进去的先执行
        // HttpServerCodec 是 HttpRequestDecoder 与 HttpResponseEncoder 的结合体
        // HttpRequestDecoder: http 请求解码器，将 channel 中的 ByteBuffer 中的数据解码为 HttpRequest 对象
        // HttpResponseEncoder: http 相应编码器，将 HttpResponse 对象编码为将要在 channel 中发送的 ByteBuffer 数据
        pipeline.addLast("HttpServerCodec", new HttpServerCodec());
        // 将自定义处理器放入到 pipeline 的最后
        pipeline.addLast(new AMServerHandler());
    }
}
