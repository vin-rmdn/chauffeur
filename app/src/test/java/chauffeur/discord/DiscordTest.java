package chauffeur.discord;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import chauffeur.discord.subscriber.SubscribeService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateMono;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class DiscordTest {
    @Mock
    MessageCreateEvent mockEvent;

    @Mock
    Message mockMessage;

    @Mock
    MessageChannel mockMessageChannel;

    @Mock
    MessageCreateMono mockMessageCreateMono;

    @Mock
    SubscribeService mockService;

    Discord classInTest;

    @BeforeEach
    void setupTest() {
        classInTest = new Discord(mockService, "fake token", 42L);
    }

    @Mock
    User mockUser;

    @Test
    void testHandleMessageCreateEvent_Failures_WhenUserIsEmpty() {
        when(mockEvent.getMessage()).thenReturn(mockMessage);
        when(mockMessage.getChannel()).thenReturn(Mono.just(mockMessageChannel));

        when(mockMessage.getAuthor()).thenReturn(Optional.empty());
        when(mockMessageChannel.createMessage("Error: no author is found")).thenReturn(mockMessageCreateMono);

        when(mockMessageCreateMono.block()).thenReturn(null);

        classInTest.handleMessageCreateEvent(mockEvent);
    }

    @Test
    void testHandleMessageCreateEvent_Failures_WhenUserIsSelf() {
        when(mockEvent.getMessage()).thenReturn(mockMessage);
        when(mockMessage.getChannel()).thenReturn(Mono.just(mockMessageChannel));

        when(mockMessage.getAuthor()).thenReturn(Optional.of(mockUser));
        when(mockUser.getId()).thenReturn(Snowflake.of(42L));

        classInTest.handleMessageCreateEvent(mockEvent);
    }

    @Test
    void testHandleMessageCreateEvent_Failures_WhenSubscriptionFails() throws SQLException {
        when(mockEvent.getMessage()).thenReturn(mockMessage);
        when(mockMessage.getChannel()).thenReturn(Mono.just(mockMessageChannel));
        when(mockMessage.getAuthor()).thenReturn(Optional.of(mockUser));
        when(mockUser.getId()).thenReturn(Snowflake.of(123L));

        when(mockMessage.getContent()).thenReturn("!subscribe");
        when(mockMessage.getAuthor()).thenReturn(Optional.of(mockUser));

        Snowflake mockUserId = Snowflake.of(123L);
        when(mockUser.getId()).thenReturn(mockUserId);

        doThrow(new SQLException("unit test-provided exception")).when(mockService).subscribe(mockUserId);
        when(mockMessageChannel.createMessage("Failed to subscribe: unit test-provided exception"))
                .thenReturn(mockMessageCreateMono);

        when(mockMessageCreateMono.block()).thenReturn(null);

        classInTest.handleMessageCreateEvent(mockEvent);

        verify(mockMessageChannel).createMessage("Failed to subscribe: unit test-provided exception");
        verify(mockService).subscribe(mockUserId);
    }

    @Test
    void testHandleMessageCreateEvent_Successful_WhenSubscriptionGoesThrough() throws SQLException {
        when(mockEvent.getMessage()).thenReturn(mockMessage);
        when(mockMessage.getChannel()).thenReturn(Mono.just(mockMessageChannel));
        when(mockMessage.getAuthor()).thenReturn(Optional.of(mockUser));
        when(mockUser.getId()).thenReturn(Snowflake.of(123L));

        when(mockMessage.getContent()).thenReturn("!subscribe");
        when(mockMessage.getAuthor()).thenReturn(Optional.of(mockUser));

        Snowflake mockUserId = Snowflake.of(123L);
        when(mockUser.getId()).thenReturn(mockUserId);

        when(mockMessageChannel.createMessage("Subscribed!")).thenReturn(mockMessageCreateMono);

        when(mockMessageCreateMono.block()).thenReturn(null);

        classInTest.handleMessageCreateEvent(mockEvent);

        verify(mockMessageChannel).createMessage("Subscribed!");
        verify(mockService).subscribe(mockUserId);
    }

    @Test
    void testHandleMessageCreateEvent_Successful_WhenUserSaysHi() {
        when(mockEvent.getMessage()).thenReturn(mockMessage);
        when(mockMessage.getChannel()).thenReturn(Mono.just(mockMessageChannel));
        when(mockMessage.getAuthor()).thenReturn(Optional.of(mockUser));
        when(mockUser.getId()).thenReturn(Snowflake.of(123L));

        when(mockMessage.getContent()).thenReturn("hi");
        when(mockMessageChannel.createMessage("https://nohello.net")).thenReturn(mockMessageCreateMono);

        when(mockMessageCreateMono.block()).thenReturn(null);

        classInTest.handleMessageCreateEvent(mockEvent);

        verify(mockMessageChannel).createMessage("https://nohello.net");
    }

    @Test
    void testHandleMessageCreateEvent_Successful_WhenCommandIsUnrecognizable() {
        when(mockEvent.getMessage()).thenReturn(mockMessage);
        when(mockMessage.getChannel()).thenReturn(Mono.just(mockMessageChannel));
        when(mockMessage.getAuthor()).thenReturn(Optional.of(mockUser));
        when(mockUser.getId()).thenReturn(Snowflake.of(123L));

        when(mockMessage.getContent()).thenReturn("como estas");
        when(mockMessageChannel.createMessage("Lo siento, no entiendo ese comando.")).thenReturn(mockMessageCreateMono);

        when(mockMessageCreateMono.block()).thenReturn(null);

        classInTest.handleMessageCreateEvent(mockEvent);

        verify(mockMessageChannel).createMessage("Lo siento, no entiendo ese comando.");
    }
}
