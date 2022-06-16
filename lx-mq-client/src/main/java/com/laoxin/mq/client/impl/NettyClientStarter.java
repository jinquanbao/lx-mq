package com.laoxin.mq.client.impl;

import com.laoxin.mq.client.api.MqDecoder;
import com.laoxin.mq.client.api.MqEncoder;
import com.laoxin.mq.client.exception.MqClientException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class NettyClientStarter implements Closeable {

    private final ConcurrentHashMap<InetSocketAddress, ConcurrentMap<Integer, CompletableFuture<MqClientHandler>>> pool;

    private final Bootstrap bootstrap;

    private final EventLoopGroup workGroup;

    private final MqClientImpl client;

    private final int maxConnections;

    private InetSocketAddress address;

    private static final Random random = new Random();

    public NettyClientStarter(MqClientImpl client){
        this.client = client;
        this.workGroup = new NioEventLoopGroup(new DefaultThreadFactory("mq-worker-group"));
        this.bootstrap = new Bootstrap();
        this.maxConnections = client.getClientConfiguration().getMaxConnections();
        this.pool = new ConcurrentHashMap<>();
        this.address = client.getAddress();
        afterPropertiesSet();
    }

    private void afterPropertiesSet(){
        bootstrap.group(workGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline()
                                //.addLast(new LoggingHandler(LogLevel.INFO))
                                .addLast("decoder", new MqDecoder())
                                .addLast("encoder", new MqEncoder())
                                .addLast("handler", new MqClientHandler(client));
                    }
                });
    }


    public CompletableFuture<MqClientHandler> createConnection(){
        return createConnection(this.address);
    }

    public CompletableFuture<MqClientHandler> createConnection(InetSocketAddress address){
        if (maxConnections == 0) {
            // Disable pool
            return createConnection(address, -1);
        }
        final int connectionKey = Math.abs(random.nextInt()) % maxConnections;

        return pool.computeIfAbsent(address, a -> new ConcurrentHashMap<>())
                .computeIfAbsent(connectionKey, k -> createConnection(address, connectionKey));
    }

    private CompletableFuture<MqClientHandler> createConnection(InetSocketAddress address,int connectionKey){

        final CompletableFuture<MqClientHandler> cnxFuture = new CompletableFuture();

        bootstrap.connect(address).addListener((ChannelFuture future) -> {
            if (!future.isSuccess()) {
                cnxFuture.completeExceptionally(new MqClientException(future.cause()));
                cleanupConnection(address, connectionKey, cnxFuture);
                return;
            }
            log.info("[{}] 连接broker中...", future.channel());
            future.channel().closeFuture().addListener(v -> {
                // 清除连接信息
                cleanupConnection(address, connectionKey, cnxFuture);
            });
            //等待broker发送 connected 连接
            final MqClientHandler cnx = (MqClientHandler) future.channel().pipeline().get("handler");
            cnx.connectionFuture().thenRun(() -> {
                if (log.isDebugEnabled()) {
                    log.debug("[{}] client连接到broker成功!", cnx.ctx().channel());
                }
                cnxFuture.complete(cnx);
            }).exceptionally(exception -> {
                log.warn("[{}] client连接到broker失败!!!: {}", cnx.ctx().channel(), exception.getMessage());
                cnxFuture.completeExceptionally(exception);
                cleanupConnection(address, connectionKey, cnxFuture);
                cnx.ctx().close();
                return null;
            });

        });

        return cnxFuture;

    }

    @Override
    public void close() throws IOException {
        workGroup.shutdownGracefully();
    }

    private void cleanupConnection(InetSocketAddress address, int connectionKey,
                                   CompletableFuture<MqClientHandler> connectionFuture) {
        ConcurrentMap<Integer, CompletableFuture<MqClientHandler>> map = pool.get(address);
        if (map != null) {
            map.remove(connectionKey, connectionFuture);
        }
    }
}
