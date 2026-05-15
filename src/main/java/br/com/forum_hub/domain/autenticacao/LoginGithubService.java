package br.com.forum_hub.domain.autenticacao;

import org.springframework.stereotype.Service;

@Service
public class LoginGithubService {
    public String gerarUrl(){
        return "https://github.com/login/oauth/authorize"+
                "?client_id=Ov23liblrRqqxHrW1cZa"+
                "&redirect_uri=http://localhost:8080/login/github/autorizado"+
                "&scope=read:user,user:email";
    }
}
