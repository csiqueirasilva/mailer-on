package br.on.daed.mailer.services.interceptors;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.stereotype.Component;

/**
 *
 * @author csiqueira
 */
@Component
public class PresenceChannelInterceptor extends ChannelInterceptorAdapter {
 
    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(message);
 
        // ignore non-STOMP messages like heartbeat messages
        if(sha.getCommand() == null) {
            return;
        }
        
        switch(sha.getCommand()) {
            case CONNECT:
                break;
            case DISCONNECT:
        }
    }
}