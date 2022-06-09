package com.laoxin.mq.client.command;

import com.laoxin.mq.client.exception.MqClientException;
import com.laoxin.mq.client.util.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

@Slf4j
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommandWrapper {

    private int totalLength;

    private int commandTypeLength;

    private String commandType;

    private byte[] data;

    private int crc32;

    public static CommandWrapper wrapper(BaseCommand command){
        return CommandWrapper.builder()
                .commandType(command.getCommandType())
                .data(JSONUtil.toJson(command).getBytes())
                .build()
        ;
    }

    public ByteBuffer encode(){
        //总数据长度统计，4字节
        int totalLength = 0;
        //命令长度统计，占用4字节
        int commandTypeLength = 4;
        //crc32校验和 占用字节
        int crc32Length = 4;

        totalLength +=commandTypeLength;
        totalLength +=crc32Length;

        byte[] commandTypeByte =null;
        if(commandType != null && commandType.length()>0){
            try {
                commandTypeByte = this.commandType.getBytes("utf-8");
                commandTypeLength = commandTypeByte.length;
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        //命令内容长度
        if(commandTypeByte != null){
            totalLength += commandTypeByte.length;
        }

        //消息内容长度 :header and body
        if(data != null){
            totalLength += data.length;
        }

        ByteBuffer result = ByteBuffer.allocate(totalLength+4);

        result.putInt(totalLength);
        result.putInt(commandTypeLength);

        if(commandTypeByte != null){
            result.put(commandTypeByte);
        }

        if(data != null){
            result.put(data);
        }

        if(result.hasArray()){
            CRC32 checksum = new CRC32();
            checksum.update(result.array(), 0, totalLength - crc32Length);
            crc32 = (int) checksum.getValue();
            result.putInt(crc32);
        }else {
            result.putInt(0);
        }

        result.flip();

        return result;
    }


    public boolean decode(ByteBuffer buffer) throws MqClientException {


        //跳过了 totalLength 占用的字节数
        int length = buffer.limit();

        if(length <= 8){
            byte[] bytes = new byte[length];
            buffer.get(bytes);
            String byteStr = new String(bytes);
            log.warn("buffer size less than 8 ,byteStr={}",byteStr);
            return false;
        }

        commandTypeLength = buffer.getInt()& 0xFFFFFF;


        if(commandTypeLength <= 0 || commandTypeLength>length){
            log.warn("commandTypeLength less than length ");
            throw new MqClientException("decode command type length error:"+commandTypeLength);
        }

        byte[] commandTypeData = new byte[commandTypeLength];
        buffer.get(commandTypeData);

        commandType = new String(commandTypeData);

        //
        int dataLength = length - 4 - commandTypeLength-4;

        data = new byte[dataLength];

        buffer.get(data);

        final int crc32Sum = buffer.getInt();

        if(buffer.hasArray()){
            CRC32 checksum = new CRC32();
            checksum.update(buffer.array(), 0, length - 4);
            crc32 = (int) checksum.getValue();
            if(crc32Sum != crc32){
                throw new MqClientException("message decode error,crc32 invalid :"+crc32Sum);
            }
        }

        return true;

    }

}
