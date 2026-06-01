package br.com.forum_hub.domain.usuario;

import br.com.forum_hub.domain.perfil.DadosPerfil;
import br.com.forum_hub.domain.perfil.HierarquiaService;
import br.com.forum_hub.domain.perfil.PerfilNome;
import br.com.forum_hub.domain.perfil.PerfilRepository;
import br.com.forum_hub.infra.email.EmailService;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import br.com.forum_hub.infra.seguranca.totp.TotpService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService implements UserDetailsService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private HierarquiaService hierarquiaService;

    @Autowired
    private TotpService totpService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByEmailIgnoreCaseAndVerificadoTrue(username).orElseThrow(() -> new UsernameNotFoundException("O usuário não foi encontrado!"));
    }

    @Transactional
    public Usuario cadastrar(DadosCadastroUsuario dados) {
        var usuario = criarUsuario(dados, false);
        emailService.enviarEmailVerificacao(usuario);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario cadastrarVerificado(DadosCadastroUsuario dados) {
        var usuario = criarUsuario(dados, true);
        return usuarioRepository.save(usuario);

    }

    @Transactional
    public void verificarEmail(String codigo) {
        var usuario = usuarioRepository.findByToken(codigo).orElseThrow();
        usuario.verificar();
    }

    @Transactional
    public Usuario editarPerfil(Usuario usuario, DadosEdicaoUsuario novosDados) {
        return usuario.alterarDados(novosDados);
    }

    public void alterarSenha(@Valid DadosAlteracaoSenha dados, Usuario usuario) {
        if (!passwordEncoder.matches(dados.senhaAtual(), usuario.getPassword())) {
            throw new RegraDeNegocioException("Senha digitada não confere com senha atual!");
        }
        String senhaCriptografada = passwordEncoder.encode(dados.novaSenha());
        usuario.alterarSenha(senhaCriptografada);
    }

    public void desativarUsuario(Long id, Usuario logado) {
        var usuario = usuarioRepository.findById(id).orElseThrow();

        if(hierarquiaService.usuarioNaoTemPermissoes(logado, usuario, "ROLE_ADMIN"))
            throw new AccessDeniedException("Não é possivel realizar essa operação!");

        usuario.desativar();
    }

    @Transactional
    public Usuario adicionarPerfil(Long id, @Valid DadosPerfil dados) {
        var usuario = usuarioRepository.findById(id).orElseThrow();
        var perfil = perfilRepository.findByNome(dados.perfilNome());
        usuario.adicionarPerfil(perfil);
        return usuario;
    }

    @Transactional
    public Usuario removerPerfil(Long id, @Valid DadosPerfil dados) {
        var usuario = usuarioRepository.findById(id).orElseThrow();
        var perfil = perfilRepository.findByNome(dados.perfilNome());
        usuario.removerPerfil(perfil);
        return usuario;
    }

    @Transactional
    public void reativarUsuario(Long id) {
        var usuario = usuarioRepository.findById(id).orElseThrow();
        usuario.reativar();
    }

    private Usuario criarUsuario(DadosCadastroUsuario dados, Boolean verificado) {
        var senhaCriptografada = passwordEncoder.encode(dados.senha());
        var perfil = perfilRepository.findByNome(PerfilNome.ESTUDANTE);
        return new Usuario(dados, senhaCriptografada, perfil, verificado);

    }

    @Transactional
    public String gerarQrCode(Usuario logado) {
        var secret = totpService.gerarSecret();
        logado.gerarSecret(secret);
        usuarioRepository.save(logado);

        return totpService.gerarQrCode(logado);
    }
}
