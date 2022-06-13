package com.laoxin.mq.broker.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.laoxin.mq.broker.entity.mq.SubscriptionConsumer;
import com.laoxin.mq.broker.entity.mq.SubscriptionEntity;
import com.laoxin.mq.broker.entity.mq.SubscriptionMetaDataEntity;
import com.laoxin.mq.broker.entity.mq.TopicEntity;
import com.laoxin.mq.broker.enums.StoreType;
import com.laoxin.mq.broker.mapper.mq.SubscriptionMapper;
import com.laoxin.mq.broker.mapper.mq.TopicMapper;
import com.laoxin.mq.broker.spring.SpringContext;
import com.laoxin.mq.client.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Slf4j
public class RdbMetaStore implements MetaStore{

    private final TopicMapper topicMapper;
    private final SubscriptionMapper subscriptionMapper;

    public RdbMetaStore(SpringContext context){
        this.topicMapper = context.topicMapper();
        this.subscriptionMapper = context.subscriptionMapper();
    }

    @Override
    public boolean storageType(String storageType) {
        return StoreType.rdb.name().equalsIgnoreCase(storageType);
    }

    @Override
    public void start() {
        subscriptionOffline();
    }

    @Override
    public void storeTopic(TopicMetaData topicMetaData, MetaStoreCallback callback) {
        if(!select(topicMetaData.getTenantId(),topicMetaData.getTopicName()).isPresent()){

            TopicEntity entity = TopicEntity
                    .builder()
                    .tenantId(topicMetaData.getTenantId())
                    .topicName(topicMetaData.getTopicName())
                    .type(topicMetaData.getTopicType())
                    .enable(1)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            try {
                topicMapper.insert(entity);
                log.info("topic store success {}",topicMetaData);
            }catch (DuplicateKeyException e){
                log.warn("topic duplicate key :{}",e.getMessage());
            }
        }
        callback.complete(topicMetaData,null);
    }

    private Optional<TopicEntity> select(long tenantId, String topic){
        final List<TopicEntity> list = topicMapper.selectList(Wrappers.lambdaQuery(TopicEntity.class)
                .eq(TopicEntity::getTenantId, tenantId)
                .eq(TopicEntity::getTopicName, topic)
        );
        if(list == null || list.isEmpty()){
            return Optional.empty();
        }else {
            return Optional.of(list.get(0));
        }
    }

    @Override
    public void storeSubscription(TopicMetaData topicMetaData, SubscriptionMetaData subscriptionMetaData, MetaStoreCallback callback) {

        Optional<SubscriptionEntity> optional = select(topicMetaData.getTenantId(), topicMetaData.getTopicName(), subscriptionMetaData.getSubscriptionName());

        final SubscriptionEntity entity = convert(topicMetaData, subscriptionMetaData);

        if(optional.isPresent()){

            entity.setId(optional.get().getId());
            final SubscriptionMetaDataEntity newMetaData = JSONUtil.fromJson(entity.getMetaData(), SubscriptionMetaDataEntity.class);
            final SubscriptionMetaDataEntity oldMetaData = JSONUtil.fromJson(optional.get().getMetaData(), SubscriptionMetaDataEntity.class);
            if(oldMetaData != null
                    && oldMetaData.getConsumers() !=null){
                newMetaData.getConsumers().addAll(oldMetaData.getConsumers());
            }
            entity.setMetaData(JSONUtil.toJson(newMetaData));
            entity.setVersion(optional.get().getVersion());
            subscriptionMapper.updateById(entity);

        }else {

            entity.setCreateTime(entity.getUpdateTime());
            subscriptionMapper.insert(entity);

        }

        callback.complete(subscriptionMetaData,null);

    }

    private SubscriptionEntity convert(TopicMetaData topicMetaData, SubscriptionMetaData subscriptionMetaData){
        HashSet hashSet = new HashSet();
        subscriptionMetaData.getConsumer().setSubscriptionTime(System.currentTimeMillis());
        hashSet.add(subscriptionMetaData.getConsumer());

        SubscriptionMetaDataEntity  metaDataEntity = SubscriptionMetaDataEntity.builder()
                .ackTimeOut(subscriptionMetaData.getAckTimeOut())
                .dependencyOnSubscription(subscriptionMetaData.getDependencyOnSubscription())
                .enablePush(subscriptionMetaData.isEnablePush())
                .filterExpression(subscriptionMetaData.getFilterExpression())
                .subscriptionProperties(subscriptionMetaData.getSubscriptionProperties())
                .consumers(hashSet)
                .build();

        SubscriptionEntity entity = SubscriptionEntity.builder()
                .subscriptionName(subscriptionMetaData.getSubscriptionName())
                .tenantId(topicMetaData.getTenantId())
                .topicName(topicMetaData.getTopicName())
                .type(subscriptionMetaData.getSubscriptionType())
                .version(0)
                .status(1)
                .updateTime(LocalDateTime.now())
                .metaData(JSONUtil.toJson(metaDataEntity))
                .deleted(0)
                .build();
        return entity;
    }

    private Optional<SubscriptionEntity> select(long tenantId, String topic, String subscription){
        final List<SubscriptionEntity> list = subscriptionMapper.selectList(Wrappers.lambdaQuery(SubscriptionEntity.class)
                .eq(SubscriptionEntity::getTenantId, tenantId)
                .eq(SubscriptionEntity::getTopicName, topic)
                .eq(SubscriptionEntity::getSubscriptionName,subscription)
        );
        if(list == null || list.isEmpty()){
            return Optional.empty();
        }else {
            return Optional.of(list.get(0));
        }
    }

    @Override
    public void removeSubscription(SubscriptionKey subscriptionKey, MetaStoreCallback callback) {

        final Optional<SubscriptionEntity> optional = select(subscriptionKey.getTenantId(), subscriptionKey.getTopicName(), subscriptionKey.getSubscriptionName());

        optional.ifPresent(entity->{
            SubscriptionEntity update = new SubscriptionEntity();
            update.setId(entity.getId());
            update.setStatus(0);
            update.setDeleteTime(LocalDateTime.now());
            update.setDeleted(1);
            final int i = subscriptionMapper.updateById(update);
            log.info("delete Subscription[{}] result={}",subscriptionKey,i);
        });

        callback.complete(subscriptionKey,null);
    }

    @Override
    public void removeSubscriptionConsumer(SubscriptionKey subscriptionKey, SubscriptionConsumer subscriptionConsumer, MetaStoreCallback callback) {

        final Optional<SubscriptionEntity> optional = select(subscriptionKey.getTenantId(), subscriptionKey.getTopicName(), subscriptionKey.getSubscriptionName());

        optional.ifPresent(entity->{

            final SubscriptionMetaDataEntity oldMetaData = JSONUtil.fromJson(entity.getMetaData(), SubscriptionMetaDataEntity.class);
            if(oldMetaData != null
                    && oldMetaData.getConsumers() !=null
                    && oldMetaData.getConsumers().remove(subscriptionConsumer)
                    ){
                SubscriptionEntity update = new SubscriptionEntity();
                update.setId(entity.getId());
                update.setStatus(oldMetaData.getConsumers().isEmpty()?0:entity.getStatus());
                update.setUpdateTime(LocalDateTime.now());
                update.setMetaData(JSONUtil.toJson(oldMetaData));
                update.setVersion(entity.getVersion());

                final int i = subscriptionMapper.updateById(update);
                log.info("remove Subscription[{}] consumer[{}] result={}",subscriptionKey,subscriptionConsumer,i);
            }

        });


        callback.complete(subscriptionKey,null);
    }

    private void subscriptionOffline(){
        final LambdaUpdateWrapper<SubscriptionEntity> wrapper =
                Wrappers.lambdaUpdate(SubscriptionEntity.class);

        SubscriptionEntity update = new SubscriptionEntity();
        update.setStatus(0);
        update.setUpdateTime(LocalDateTime.now());
        update.setMetaData(JSONUtil.toJson(new HashMap<>()));
        subscriptionMapper.update(update,wrapper);
    }

}
