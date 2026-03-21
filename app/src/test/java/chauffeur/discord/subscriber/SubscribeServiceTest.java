package chauffeur.discord.subscriber;

import discord4j.common.util.Snowflake;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SubscribeServiceTest {
  @Mock
  SubscribeRepository mockRepository;

  SubscribeService service;

  @BeforeEach
  void setUp() {
    service = new SubscribeService(mockRepository);
  }

  @Test
  void testSubscribe() throws SQLException {
    service.subscribe(Snowflake.of(123L));
  }
}
