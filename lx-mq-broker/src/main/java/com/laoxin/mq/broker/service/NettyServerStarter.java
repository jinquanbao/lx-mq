package com.laoxin.mq.broker.service;

import com.laoxin.mq.client.api.MqDecoder;
import com.laoxin.mq.client.api.MqEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class NettyServerStarter implements Closeable {

    private final BrokerService service;

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    private final int port;

    private final Thread thread;
    private final CompletableFuture<Void> startedFuture;

    public NettyServerStarter(BrokerService service){
        this.service = service;
        this.port = service.conf().getServerPort();
        this.startedFuture = new CompletableFuture();
        final DefaultThreadFactory bossThreadFactory = new DefaultThreadFactory("mq-boss-group");
        final DefaultThreadFactory workerThreadFactory = new DefaultThreadFactory("mq-worker-group");
        this.bossGroup = new NioEventLoopGroup(1,bossThreadFactory);
        this.workerGroup = new NioEventLoopGroup(workerThreadFactory);
        this.thread = new Thread(this::process);
        this.thread.setName("netty-server");
    }

    public CompletableFuture<Void> start(){
        this.thread.start();
        return startedFuture;
    }

    private void process(){
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    //.childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline()
                                    .addLast("decoder", new MqDecoder())
                                    .addLast("encoder", new MqEncoder())
                                    .addLast(new MqServerHandler(service));
                        }
                    })
                    ;
            // 绑定端口，开始接收进来的链接
            ChannelFuture channelFuture = serverBootstrap.bind(port).syncUninterruptibly();
            log.info("broker 启动完成，监听【" + port + "】端口");
            startedFuture.complete(null);
            channelFuture.channel().closeFuture().syncUninterruptibly();
        } catch (Exception e) {
            log.error("broker 启动异常", e);
            this.startedFuture.completeExceptionally(e);
            throw new RuntimeException("broker 启动异常",e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }


    @Override
    public void close() throws IOException {
        log.info("netty server closing...");
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        log.info("netty server closed!");
    }
}
