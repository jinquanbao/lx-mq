package com.laoxin.mq.broker.position;

import com.laoxin.mq.broker.Application;
import com.laoxin.mq.broker.service.BrokerService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

@SpringBootTest(classes = Application.class)
@RunWith(SpringRunner.class)
public class RdbPositionOffsetStoreTest {

    @Autowired
    private BrokerService brokerService;

    @Test
    public void persistTest(){

        final PositionKey positionKey = PositionKey.builder()
                .topic("test")
                .subscription("subscription3")
                .tenantId(1)
                .build();

        Position position = Position.builder()
                .positionKey(positionKey)
                .entryId(10)
                .build();

        Assert.assertTrue(brokerService.positionOffsetStore().persist(position));

        position.setEntryId(1);

        Assert.assertFalse(brokerService.positionOffsetStore().persist(position));

        position.setEntryId(15);

        Assert.assertTrue(brokerService.positionOffsetStore().persist(position));

        final Optional<Position> optional = brokerService.positionOffsetStore().getPosition(positionKey);

        Assert.assertEquals(15,optional.get().getEntryId());

    }

    @Test
    public void seekTest(){

        final PositionKey positionKey = PositionKey.builder()
                .topic("test")
                .subscription("subscription4")
                .tenantId(1)
                .build();

        Position position = Position.builder()
                .positionKey(positionKey)
                .entryId(10)
                .build();

        Assert.assertTrue(brokerService.positionOffsetStore().seek(position));

        position.setEntryId(15);

        Assert.assertTrue(brokerService.positionOffsetStore().seek(position));

        position.setEntryId(1);

        Assert.assertTrue(brokerService.positionOffsetStore().seek(position));

        final Optional<Position> optional = brokerService.positionOffsetStore().getPosition(positionKey);

        Assert.assertEquals(1,optional.get().getEntryId());

    }
}
