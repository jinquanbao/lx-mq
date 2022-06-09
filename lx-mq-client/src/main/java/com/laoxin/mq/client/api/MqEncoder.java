package com.laoxin.mq.client.api;

import com.laoxin.mq.client.command.CommandWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
@ChannelHandler.Sharable
public class MqEncoder extends MessageToByteEncoder<CommandWrapper> {
    @Override
    protected void encode(ChannelHandlerContext ctx, CommandWrapper wrapper, ByteBuf out) throws Exception {
        try {
            ByteBuffer buffer = wrapper.encode();
            out.writeBytes(buffer);
        } catch (Exception e) {
            log.error("encode exception, " + ctx.channel(), e);
            ctx.close();
        }
    }
}
