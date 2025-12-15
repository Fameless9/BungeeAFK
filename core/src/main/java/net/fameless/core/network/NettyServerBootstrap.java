package net.fameless.core.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import net.fameless.core.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class NettyServerBootstrap {

    private static final Logger logger = LoggerFactory.getLogger("BungeeAFK/NettyServerBootstrap");
    private static final EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
    private static final EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(Runtime.getRuntime().availableProcessors(), NioIoHandler.newFactory());

    public static void initializeServer() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline().addLast(
                                new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 4),
                                new LengthFieldPrepender(4),
                                new StringDecoder(StandardCharsets.UTF_8),
                                new StringEncoder(StandardCharsets.UTF_8),
                                new InboundChannelHandler()
                        );
                    }
                });

        int port = Config.getInstance().getInt("netty-port", 9000);
        bootstrap.bind(port).sync();
        logger.info("Listening on port: {}", port);
    }

    public static void shutdownServer() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}
