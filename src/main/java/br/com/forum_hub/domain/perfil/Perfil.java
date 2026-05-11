package br.com.forum_hub.domain.perfil;

import jakarta.persistence.*;

@Entity
@Table(name = "perfis")
public class Perfil {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private PerfilNome nome;
}
