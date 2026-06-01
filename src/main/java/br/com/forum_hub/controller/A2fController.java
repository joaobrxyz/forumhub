package br.com.forum_hub.controller;

import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.domain.usuario.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class A2fController {
    @Autowired
    private UsuarioService usuarioService;

    @PatchMapping("configurar-a2f")
    public ResponseEntity<String> gerarQrCode(@AuthenticationPrincipal Usuario logado){
        var url = usuarioService.gerarQrCode(logado);
        return ResponseEntity.ok(url);
    }

    @PatchMapping("ativar-a2f")
    public ResponseEntity<Void> ativarA2f(@RequestParam String codigo, @AuthenticationPrincipal Usuario logado){
        usuarioService.ativarA2f(codigo, logado);
        return ResponseEntity.noContent().build();
    }
}
