package com.laoxin.mq.client.api;

import com.laoxin.mq.client.command.CommandWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
public class MqDecoder extends LengthFieldBasedFrameDecoder {

    private static final int FRAME_MAX_LENGTH =
            Integer.parseInt(System.getProperty("mq.frameMaxLength", "16777216"));

    public MqDecoder() {
        super(FRAME_MAX_LENGTH, 0, 4, 0, 4);
    }

    @Override
    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = null;
        try {
            frame = (ByteBuf) super.decode(ctx, in);
            if (null == frame) {
                return null;
            }

            ByteBuffer byteBuffer = frame.nioBuffer();

            CommandWrapper wrapper = new CommandWrapper();

            if(wrapper.decode(byteBuffer)){
                return wrapper;
            }
        } catch (Exception e) {
            log.error("decode exception, " + ctx.channel(), e);
            ctx.close();
        } finally {
            if (null != frame) {
                frame.release();
            }
        }

        return null;
    }
}
