package br.com.forum_hub.domain.autenticacao.google;

import br.com.forum_hub.domain.autenticacao.github.DadosEmail;
import br.com.forum_hub.domain.usuario.DadosCadastroUsuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final String redirectUriRegistro = "http://localhost:8080/login/google/registro-autorizado";

    public LoginGoogleService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public String gerarUrl(){
        return "https://accounts.google.com/o/oauth2/v2/auth"+
                "?client_id="+clientId+
                "&redirect_uri="+redirectUri+
                "&scope=https://www.googleapis.com/auth/userinfo.email" +
                "&response_type=code" +

                "&access_type=offline";
    }

    private Map obterTokens(String code, String uri) {
        var resposta = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of("code", code, "client_id", clientId,
                        "client_secret", clientSecret, "redirect_uri", uri, "grant_type", "authorization_code"))
                .retrieve()
                .body(Map.class);
        return resposta;
    }

    public String obterEmail(String code){
        var tokens = obterTokens(code, redirectUri);
        var idToken = tokens.get("id_token");
        var refreshToken = tokens.get("refresh_token");

        if (refreshToken != null) {
            // Armazene o refresh token de forma segura no banco de dados
            System.out.println("Refresh Token: " + refreshToken);
        }

        var decodedJWT = JWT.decode(idToken.toString());
        return decodedJWT.getClaim("email").asString();
    }

    public String gerarUrlRegistro() {
        return "https://accounts.google.com/o/oauth2/v2/auth"+
                "?client_id="+clientId +
                "&redirect_uri="+redirectUriRegistro +
                "&scope=https://www.googleapis.com/auth/userinfo.email" +
                "%20https://www.googleapis.com/auth/userinfo.profile" +
                "&response_type=code";
    }

    public DadosCadastroUsuario obterDadosOAuth(String codigo){
        var tokens = obterTokens(codigo, redirectUriRegistro);
        var idToken = tokens.get("id_token").toString();
        System.out.println(tokens);
        var decodedJWT = JWT.decode(idToken);
        var email = decodedJWT.getClaim("email").asString();
        var senha = UUID.randomUUID().toString();
        var nomeCompleto = decodedJWT.getClaim("name").asString();
        var nomeUsuario = email.split("@")[0]; //pega a parte que vem antes do '@' no email

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


    public String renovarAccessToken(String refreshToken) {
        var resposta = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "refresh_token", refreshToken,
                        "grant_type", "refresh_token"
                ))
                .retrieve()
                .body(Map.class);
        return resposta.get("access_token").toString();
    }
}
