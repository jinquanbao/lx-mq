package com.laoxin.mq.client.command;

import com.google.protobuf.ByteString;
import com.laoxin.mq.client.enums.CommandType;
import com.laoxin.mq.client.util.JSONUtil;
import com.laoxin.mq.protos.BaseCommandProto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BaseCommand {

    private String commandType;

    private Map<String,Object> header;

    private String body;

    public ByteBuf toByteBufMessage(){
        return Unpooled.copiedBuffer(JSONUtil.toJson(this).getBytes());
    }

    public CommandWrapper toCommandWrapper(){
        String tmp = this.commandType;
        this.commandType = null;
        return CommandWrapper.builder()
                .commandType(tmp)
                .data(JSONUtil.toJson(this).getBytes())
                .build()
                ;
    }

    public BaseCommandProto.BaseCommand toProtoCommand(){
        String tmp = this.commandType;
        this.commandType = null;
        return BaseCommandProto.BaseCommand
                .newBuilder()
                .setCommandType(CommandType.getEnum(tmp).getType())
                .setMessage(ByteString.copyFromUtf8(JSONUtil.toJson(this)))
                .build();
    }


    private static final String REQUEST_ID =  "x-request-id";
    private static final String AUTH_CLIENT_ID =  "x-auth-client-id";

    public BaseCommand requestId(long requestId){
        if(header == null){
            header = new HashMap<>();
        }
        header.put(REQUEST_ID,requestId);
        return this;
    }

    public BaseCommand authClientId(String authClientId){
        if(header == null){
            header = new HashMap<>();
        }
        header.put(AUTH_CLIENT_ID,authClientId);
        return this;
    }

    public long getRequestId(){
        final Object o = header == null?null:header.get(REQUEST_ID);
        return o == null?0:Long.parseLong(o.toString());
    }

    public String getAuthClientId(){
        final Object o = header == null?null:header.get(AUTH_CLIENT_ID);
        return o == null?null:(String)o;
    }

}
