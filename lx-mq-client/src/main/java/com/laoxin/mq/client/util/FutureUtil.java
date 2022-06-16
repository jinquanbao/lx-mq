package com.laoxin.mq.client.util;

import com.laoxin.mq.client.api.Message;
import com.laoxin.mq.client.api.MessageBuilder;
import com.laoxin.mq.client.impl.MessageIdImpl;
import com.laoxin.mq.client.impl.MessageImpl;
import org.springframework.core.ParameterizedTypeReference;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class FutureUtil {

    public static <T> CompletableFuture<T> failedFuture(Throwable t) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(t);
        return future;
    }

    public static <T> boolean futureSuccess(CompletableFuture<T> future) {
        if (future != null && future.isDone() && !future.isCompletedExceptionally()) {
            return true;
        }
        return false;
    }

    public static <T> boolean futureFailed(CompletableFuture<T> future) {
        if (future != null && future.isDone() && future.isCompletedExceptionally()) {
            return true;
        }
        return false;
    }

    public static <T> CompletableFuture<T> waitForAll(List<CompletableFuture<T>> futures) {
        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        final CompletableFuture<T> compositeFuture = new CompletableFuture<>();
        final AtomicInteger count = new AtomicInteger(futures.size());
        final AtomicReference<Throwable> exception = new AtomicReference<>();

        for (CompletableFuture<T> future : futures) {
            future.whenComplete((r, ex) -> {
                if (ex != null) {
                    exception.compareAndSet(null, ex);
                }
                if (count.decrementAndGet() == 0) {
                    //all futures completed
                    if (exception.get() != null) {
                        compositeFuture.completeExceptionally(exception.get());
                    } else {
                        compositeFuture.complete(null);
                    }
                }
            });
        }

        return compositeFuture;
    }

    public static void main(String[] args) {
        final Message builder = MessageBuilder.create().setContent(new User())
                .setMessageId(new MessageIdImpl(1,"xx",1))
                .build();

        List<Message> list = new ArrayList<>();
        list.add(builder);
        
        final String s = JSONUtil.toJson(list);

        ParameterizedTypeImpl type = ParameterizedTypeImpl.make(MessageImpl.class, new Class[]{User.class}, null);

        type = ParameterizedTypeImpl.make(List.class,new ParameterizedTypeImpl[]{type},null);

        List<Message<User>> message = JSONUtil.fromJson(s, type);
        List<Message<User>> message2 = JSONUtil.fromJsonArray(s, MessageImpl.class);

        System.out.println(message);

    }



    static class User{
        public int id;
        public String name;
    }
}
