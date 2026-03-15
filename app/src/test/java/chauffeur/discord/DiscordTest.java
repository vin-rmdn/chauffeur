package chauffeur.discord;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateMono;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class DiscordTest {
    Discord classInTest = new Discord();

    @Mock
    MessageCreateEvent mockEvent;

    @Mock
    Message mockMessage;

    @Mock
    MessageChannel mockMessageChannel;

    @Mock
    MessageCreateMono mockMessageCreateMono;

    @Test
    void testHandleMessageCreateEvent_Successful_WhenUserSaysHi() {
        when(mockEvent.getMessage()).thenReturn(mockMessage);
        when(mockMessage.getChannel()).thenReturn(Mono.just(mockMessageChannel));
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
        when(mockMessage.getContent()).thenReturn("como estas");
        when(mockMessageChannel.createMessage("Lo siento, no entiendo ese comando.")).thenReturn(mockMessageCreateMono);
        
        when(mockMessageCreateMono.block()).thenReturn(null);
        
        classInTest.handleMessageCreateEvent(mockEvent);

        verify(mockMessageChannel).createMessage("Lo siento, no entiendo ese comando.");
    }
}
