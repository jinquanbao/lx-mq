/**
 * Copyright 2016 Yahoo Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.laoxin.mq.client.enums;

import java.util.Arrays;

/**
 * Types of topic supported
 */
public enum TopicType {
    Default,
    Binlog,
    ;
    public static TopicType getEnum(String topicType){
        return Arrays.stream(TopicType.values())
                .filter(x->x.name().equals(topicType))
                .findAny()
                .orElse(null);
    }
}