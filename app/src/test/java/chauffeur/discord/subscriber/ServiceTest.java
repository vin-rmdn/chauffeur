package chauffeur.discord.subscriber;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import discord4j.common.util.Snowflake;

@ExtendWith(MockitoExtension.class)
public class ServiceTest {
    @Mock
    Repository mockRepository;

    Service service;

    @BeforeEach
    void setUp() {
        service = new Service(mockRepository);
    }

    @Test
    void testSubscribe() throws SQLException {
        service.subscribe(Snowflake.of(123L));
    }
}
