package br.com.forum_hub.controller;

import br.com.forum_hub.domain.autenticacao.DadosToken;
import br.com.forum_hub.domain.autenticacao.TokenService;
import br.com.forum_hub.domain.autenticacao.github.LoginGithubService;
import br.com.forum_hub.domain.autenticacao.google.LoginGoogleService;
import br.com.forum_hub.domain.usuario.UsuarioRepository;
import br.com.forum_hub.domain.usuario.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/login/google")
public class LoginGoogleController {
    @Autowired
    private LoginGoogleService loginGoogleService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<Void> redrirecionarGoogle() {
        var url = loginGoogleService.gerarUrl();
        var headers = new HttpHeaders();
        headers.setLocation(URI.create(url));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/autorizado")
    public ResponseEntity<DadosToken> autenticarUsuarioOAuth(@RequestParam String code){
        var email = loginGoogleService.obterEmail(code);

        var usuario = usuarioRepository.findByEmailIgnoreCaseAndVerificadoTrueAndAtivoTrue(email).orElseThrow();

        var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String tokenAcesso = tokenService.gerarToken( usuario);
        String refreshTokenAtt = tokenService.gerarRefreshToken(usuario);

        return ResponseEntity.ok(new DadosToken(tokenAcesso, refreshTokenAtt, false));
    }

    @GetMapping("/registro")
    public ResponseEntity<Void> redirecionarRegistroGoogle(){
        var url = loginGoogleService.gerarUrlRegistro();
        var headers = new HttpHeaders();
        headers.setLocation(URI.create(url));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/registro-autorizado")
    public ResponseEntity<DadosToken> registrarOAuth(@RequestParam String code){
        var dadosUsuario = loginGoogleService.obterDadosOAuth( code);
        var usuario = usuarioService.cadastrarVerificado(dadosUsuario);
        var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String tokenAcesso = tokenService.gerarToken(usuario);
        String refreshToken = tokenService.gerarRefreshToken(usuario);
        return ResponseEntity.ok(new DadosToken(tokenAcesso, refreshToken, false));
    }
}
