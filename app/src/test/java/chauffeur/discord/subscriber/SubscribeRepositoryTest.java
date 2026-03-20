package chauffeur.discord.subscriber;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
public class SubscribeRepositoryTest {
    @Mock
    JdbcTemplate mockTemplate;

    SubscribeRepository repository;

    @BeforeEach
    void setupTest() {
        repository = new SubscribeRepository(mockTemplate);
    }

    @Test
    void testSave_Failure_SQLException() {
        when(mockTemplate.update("INSERT INTO subscribers (user_id) VALUES (?);", 123L))
                .thenThrow(new DataAccessException("unit test-provided exception") {
                });

        assertThrows(DataAccessException.class, () -> repository.save(123L));
    }

    @Test
    void testSave_Successful() {
        when(mockTemplate.update("INSERT INTO subscribers (user_id) VALUES (?);", 123L)).thenReturn(1);

        repository.save(123L);
    }
}
