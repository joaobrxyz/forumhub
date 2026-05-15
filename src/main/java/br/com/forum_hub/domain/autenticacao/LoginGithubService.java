package br.com.forum_hub.domain.autenticacao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class LoginGithubService {
    private final String clientId = "Ov23liblrRqqxHrW1cZa";
    private final String clientSecret = "1a2e8f57dfc6d63503243093e3bff9c91ca274d4";
    private final String redirectUri = "http://localhost:8080/login/github/autorizado";

    @Autowired
    private RestClient.Builder restClient;

    public String gerarUrl(){
        return "https://github.com/login/oauth/authorize"+
                "?client_id="+clientId+
                "&redirect_uri="+redirectUri+
                "&scope=read:user,user:email";
    }

    public String obterToken(String code) {
        var resposta = restClient.build().post()
                .uri("https://github.com/login/oauth/access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of("code", code, "client_id", clientId, "client_secret", clientSecret, "redirect_uri", redirectUri))
                .retrieve()
                .body(String.class);
        return resposta;
    }
}
