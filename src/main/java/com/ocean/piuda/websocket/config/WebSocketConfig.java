package com.ocean.piuda.websocket.config;

import com.ocean.piuda.websocket.handler.StompJwtChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompJwtChannelInterceptor stompJwtChannelInterceptor;

    @Value("${stomp.relay.host:localhost}")
    private String relayHost;

    @Value("${stomp.relay.port:61613}")
    private Integer relayPort;

    @Value("${stomp.relay.login:guest}")
    private String relayLogin;

    @Value("${stomp.relay.passcode:guest}")
    private String relayPasscode;



    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // ws://.../ws
                .setAllowedOriginPatterns(
                        "http://localhost:*",
                        "https://localhost:*",
                        "http://127.0.0.1:*",
                        "https://127.0.0.1:*",
                        "https://어쩌구저쩌구.vercel.app",
                        "https://www.jungjiyu.com",
                        "http://www.jungjiyu.com"
                )
                .withSockJS(); // 필요 없으면 제거 가능
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트 → 서버로 보내는 prefix
        registry.setApplicationDestinationPrefixes("/pub");

        // 서버 → RabbitMQ STOMP 릴레이 (구독 prefix)
        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(relayHost)
                .setRelayPort(relayPort)
                .setClientLogin(relayLogin)
                .setClientPasscode(relayPasscode);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // STOMP CONNECT 시 JWT 인증 처리
        registration.interceptors(stompJwtChannelInterceptor);
    }
}
