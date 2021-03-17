package de.exbio.reposcapeweb.configs;

import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    Environment env;

//    @Override
//    @CrossOrigin(origins="http://localhost:8024")
//    public void configureMessageBroker(MessageBrokerRegistry registry) {
//        registry.enableSimpleBroker("/graph/status"/*, "/stats","/leaderboard"*/);
//        registry.setApplicationDestinationPrefixes("/socket");
//        registry.setUserDestinationPrefix("/user");
//    }

    @Override
//    @CrossOrigin(origins="http://localhost:8024")
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/jobs").setAllowedOrigins(env.getProperty("server.allowedOrigin")).withSockJS();
    }
}
