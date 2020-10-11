package cn.lizhaoloveit.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;

/**
 * DESCRIPTION:
 * Author: ammar
 * Date:   2020-10-10
 * Time:   06:49
 */
public class WebSocketServer implements AMHttpService, AMWebSocketService {

    public static void main(String[] args) {
        new WebSocketServer("192.168.0.7", 8888).start();
    }

    /**
     * 握手用的变量
     */
    private static final AttributeKey<WebSocketServerHandshaker> ATTR_HAND_SHAKER = AttributeKey.newInstance("ATTR_KEY_CHANNEL_ID");
    private static final int MAX_CONTENT_LENGTH = 65536;

    /**
     * 请求类型常量
     */
    private static final String WEBSOCKET_UPGRADE = "websocket";
    private static final String WEBSOCKET_CONNECTION = "Upgrade";
    private static final String WEBSOCKET_URI_ROOT_PATTERN = "ws://%s:%d";
    private final String WEBSOCKET_URI_ROOT;
    /**
     * 用户字段
     */
    private String host;
    private int port;

    public WebSocketServer(String host, int port) {
        this.host = host;
        this.port = port;
        WEBSOCKET_URI_ROOT = String.format(WEBSOCKET_URI_ROOT_PATTERN, host, port);
    }

    public void start() {
        // 实例化 nio 监听事件池
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 实例化 nio 工作线程池
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        // 启动器
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(new ChunkedWriteHandler());
                pipeline.addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH));
                // 3 秒内服务器没有发生读操作，则会触发读操作空闲事件 code 0 to disable
                // 5 秒内服务器没有发生写操作，则会触发读操作空闲事件 code 0 to disable
                // x 秒内服务器没有读或没有写操作，则会触发事件，code 0 to disable
                pipeline.addLast(new  IdleStateHandler(30, 0, 0));
                // 设置 websocket 服务处理方式
                pipeline.addLast(new AMServerHandler(WebSocketServer.this, WebSocketServer.this));
            }
        });
        /**
         * 实例化后，要完成端口绑定
         */
        try {
            ChannelFuture channelFuture = bootstrap.bind(host, port).addListener((ChannelFutureListener) cf -> {
                if (cf.isSuccess()) {
                    System.out.println("webSocket started");
                }
            }).sync();
            channelFuture.channel().closeFuture().addListener((ChannelFutureListener) cf -> {
                System.out.println("server channel" + cf.channel().remoteAddress() + "closed");
            }).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        System.out.println("webSocket shutdown");
    }

    @Override
    public void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        // 判断是不是 socket 请求
        if (isWebSocketUpgrade(request)) {
            // 是 webSocket 请求
            System.out.println("请求时 webSocket 协议");
            String subProtocols = request.headers().get(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL);
            // 握手 设置 uri + 协议 + 不允许扩展
            WebSocketServerHandshakerFactory handshakerFactory = new WebSocketServerHandshakerFactory(WEBSOCKET_URI_ROOT, subProtocols, false);
            WebSocketServerHandshaker handshaker = handshakerFactory.newHandshaker(request);
            if (handshaker == null) {
                // 握手失败：不支持的协议
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                // 相应请求：将握手转交 channel 处理
                handshaker.handshake(ctx.channel(), request);
                ctx.channel().attr(ATTR_HAND_SHAKER).set(handshaker);
            }
        }
        // 不处理 HTTP 请
    }

    @Override
    public void handleFrame(ChannelHandlerContext ctx, WebSocketFrame frame, ChannelGroup group) {
        if (frame instanceof TextWebSocketFrame) {
            String text = ((TextWebSocketFrame) frame).text();
            TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(ctx.channel().remoteAddress() + ":" + text);
            System.out.println("receive textWebSocketFrame from channel: "
                    + ctx.channel().remoteAddress()
                    + "，目前一共" + group.size() + "个在线");
            // 发送给其他的 channel
            group.remove(ctx.channel());
            System.out.println(ctx.channel().remoteAddress() + "发送了消息" + text);
            group.writeAndFlush(textWebSocketFrame);
            group.add(ctx.channel());
        }

        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            System.out.println("receive pingWebSocket from channel:" + ctx.channel().remoteAddress());
            return;
        }

        if (frame instanceof PongWebSocketFrame) return;
        if (frame instanceof CloseWebSocketFrame) {
            // 获取到握手信息
            WebSocketServerHandshaker handshaker = ctx.channel().attr(ATTR_HAND_SHAKER).get();
            if (handshaker == null) {
                System.out.println("channel:" + ctx.channel().remoteAddress() + "has no handShaker");
                return;
            }
            handshaker.close(ctx.channel(), ((CloseWebSocketFrame) frame).retain());
            return;
        }
        /**
         * 二进制 frame 忽略
         */
    }

    /**
     * 判断是否是 webSocket 请求
     *
     * @param request
     * @return
     */
    private boolean isWebSocketUpgrade(FullHttpRequest request) {
        HttpHeaders headers = request.headers();
        return request.method().equals(HttpMethod.GET)
                && headers.get(HttpHeaderNames.UPGRADE).contains(WEBSOCKET_UPGRADE)
                && headers.get(HttpHeaderNames.CONNECTION).contains(WEBSOCKET_CONNECTION);
    }
}