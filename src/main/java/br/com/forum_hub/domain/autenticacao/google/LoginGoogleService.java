package br.com.forum_hub.domain.autenticacao.google;

import br.com.forum_hub.domain.autenticacao.github.DadosEmail;
import br.com.forum_hub.domain.usuario.DadosCadastroUsuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Service
public class LoginGoogleService {
    @Value("${google.oauth.client.id}")
    private String clientId;

    @Value("${google.oauth.client.secret}")
    private String clientSecret;

    private final String redirectUri = "http://localhost:8080/login/google/autorizado";
    private final RestClient restClient;

    public LoginGoogleService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public String gerarUrl(){
        return "https://accounts.google.com/o/oauth2/v2/auth"+
                "?client_id="+clientId+
                "&redirect_uri="+redirectUri+
                "&scope=https://www.googleapis.com/auth/userinfo.email" +
                "&response_type=code";
    }

    private String obterToken(String code, String id, String uri) {
        var resposta = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of("code", code, "client_id", id,
                        "client_secret", clientSecret, "redirect_uri", uri, "grant_type", "authorization_code"))
                .retrieve()
                .body(Map.class);
        return resposta.get("id_token").toString();
    }

    public String obterEmail(String code){
        var token = obterToken(code, clientId, redirectUri);
        System.out.println(token);

        var decodedJWT = JWT.decode(token);
        System.out.println(decodedJWT.getClaims());
        return decodedJWT.getClaim("email").asString();
    }

    public String gerarUrlRegistro() {
        return "https://github.com/login/oauth/authorize"+
                "?client_id="+clientId +
                "&redirect_uri="+redirectUri +
                "&scope=read:user,user:email";
    }

    public DadosCadastroUsuario obterDadosOAuth(String codigo){
        var accessToken = obterToken(codigo,  clientId, redirectUri);
        var headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        var email = enviarRequisicaoEmail(headers);
        var resposta = restClient.get()
                .uri("https://api.github.com/user")
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Map.class);

        var nomeCompleto = resposta.get("name").toString();
        var nomeUsuario = resposta.get("login").toString();
        var senha = UUID.randomUUID().toString();
        return new DadosCadastroUsuario(email, senha, nomeCompleto, nomeUsuario, null, null);
    }



    private String enviarRequisicaoEmail(HttpHeaders headers) {
        var resposta = restClient.get()
                .uri("https://api.github.com/user/emails")
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(DadosEmail[].class);

        for(DadosEmail d: resposta){
            if(d.primary() && d.verified())
                return d.email();
        }
        return null;
    }
}
