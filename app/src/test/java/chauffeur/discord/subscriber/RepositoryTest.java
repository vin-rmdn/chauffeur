package chauffeur.discord.subscriber;

import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RepositoryTest {
    @Mock
    Connection mockConnection;

    @Mock
    PreparedStatement mockStatement;

    Repository repository;

    @BeforeEach
    void setupTest() {
        repository = new Repository(mockConnection);
    }

    @Test
    void testSave_Failure_SQLException() throws SQLException {
        when(mockConnection.prepareStatement("INSERT INTO subscribers (id) VALUES (?)")).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

        repository.save(123L);
    }

    @Test
    void testSave_Successful() throws SQLException {
        when(mockConnection.prepareStatement("INSERT INTO subscribers (id) VALUES (?)")).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        repository.save(123L);
    }
}
