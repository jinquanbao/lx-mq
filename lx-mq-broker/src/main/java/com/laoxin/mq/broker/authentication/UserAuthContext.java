package com.laoxin.mq.broker.authentication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserAuthContext {

    public static ThreadLocal<UserAuthContext> current = new ThreadLocal<UserAuthContext>(){
        protected synchronized UserAuthContext initialValue() {
            return new UserAuthContext();
        }
    };

    private String clientId;

    private long tenantId;

    private Set<String> scope;

    public boolean scopeAll(){
        return scope != null && scope.contains("all");
    }

    public static String getCurrentClientId(){
        return UserAuthContext.current.get().clientId;
    }

    public static long getCurrentTenantId(){
        return UserAuthContext.current.get().tenantId;
    }
    public static Set<String> getCurrentScope(){
        return UserAuthContext.current.get().scope;
    }
}
