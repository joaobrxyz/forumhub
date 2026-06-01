package br.com.forum_hub.domain.usuario;

import br.com.forum_hub.domain.perfil.Perfil;
import br.com.forum_hub.domain.perfil.PerfilNome;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nomeCompleto;
    private String email;
    private String senha;
    private String nomeUsuario;
    private String biografia;
    private String miniBiografia;
    private Boolean verificado;
    private String token;
    private LocalDateTime expiracaoToken;
    private Boolean ativo;
    private String secret;
    private Boolean a2fAtiva;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuarios_perfis", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "perfil_id"))
    private List<Perfil> perfis = new ArrayList<>();

    public Usuario(){}

    public Usuario(DadosCadastroUsuario dados, String senhaCriptografada, Perfil perfil, Boolean verificado) {
        this.nomeCompleto = dados.nomeCompleto();
        this.email = dados.email();
        this.senha = senhaCriptografada;
        this.nomeUsuario = dados.nomeUsuario();
        this.biografia = dados.biografia();
        this.miniBiografia = dados.miniBiografia();

        if(verificado){
            aprovarUsuario();
        } else {
            this.verificado = false;
            this.token = UUID.randomUUID().toString();
            this.expiracaoToken = LocalDateTime.now().plusMinutes(30);
            this.ativo = false;
        }
        this.perfis.add(perfil);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return perfis;
    }

    public void verificar() {
        if(expiracaoToken.isBefore(LocalDateTime.now())){
            throw new RegraDeNegocioException("Link de verificação expirou!");
        }
        aprovarUsuario();
    }

    private void aprovarUsuario(){
        this.verificado = true;
        this.ativo = true;
        this.token = null;
        this.expiracaoToken = null;
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public String getBiografia() {
        return biografia;
    }

    public String getMiniBiografia() {
        return miniBiografia;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    public void setMiniBiografia(String miniBiografia) {
        this.miniBiografia = miniBiografia;
    }

    public Usuario alterarDados(DadosEdicaoUsuario novosDados) {
        if (novosDados.nomeUsuario() != null) {
            this.nomeUsuario = novosDados.nomeUsuario();
        }
        if (novosDados.miniBiografia() != null) {
            this.nomeUsuario = novosDados.miniBiografia();
        }
        if (novosDados.biografia() != null) {
            this.nomeUsuario = novosDados.biografia();
        }
        return this;
    }

    public void alterarSenha(String senhaCriptografada) {
        this.senha = senhaCriptografada;
    }

    public void desativar() {
        this.ativo = false;
    }

    public void reativar() {
        this.ativo = true;
    }

    public void adicionarPerfil(Perfil perfil) {
        this.perfis.add(perfil);
    }

    public void removerPerfil(Perfil perfil) {
        this.perfis.remove(perfil);
    }

    public void gerarSecret(String secret) {
        this.secret = secret;
    }

    public String getSecret() {
        return this.secret;
    }

    public boolean isA2fAtiva() {
        return this.a2fAtiva;
    }

    public void ativarA2f() {
        this.a2fAtiva = true;
    }
}
