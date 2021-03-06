package de.exbio.reposcapeweb.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    @CrossOrigin
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/graph/status"/*, "/stats","/leaderboard"*/);
        registry.setApplicationDestinationPrefixes("/socket");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    @CrossOrigin
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/jobs").setAllowedOrigins("*");//.withSockJS();
    }
}
