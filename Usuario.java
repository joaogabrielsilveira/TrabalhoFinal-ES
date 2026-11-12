import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

public class Usuario {

    private static int ID = 0;
    private final int matricula;
    private String nome;
    private String email;
    private String senha;
    private TipoUsuario type;
    // As mensagens NÃO ficam mais aqui: existem uma única vez, no Sistema.

    public Usuario(int matricula, String nome, String email, String senha, TipoUsuario tipo) {
        this.matricula = ID;
        ID++;
        this.nome = nome;
        this.email = email;
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        this.senha = encoder.encode(senha);
        this.type = tipo;
    }

    /** Chamado pelo Sistema ao carregar o banco, para não repetir matrículas. */
    public static void restaurarContador(int proximo) {
        ID = proximo;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Usuario outro) && this.matricula == outro.matricula;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(matricula);
    }

    public void enviarMensagem(String titulo, String conteudo, Usuario destinatario) {
        Mensagem nova = new Mensagem(titulo, conteudo, this, destinatario);
        Sistema.registrarMensagem(nova);
    }

    public List<Mensagem> getMensagensRecebidas() {
        return Sistema.getMensagensRecebidas(this.email);
    }

    public List<Mensagem> getMensagensEnviadas() {
        return Sistema.getMensagensEnviadas(this.email);
    }

    public String getEmail() {
        return this.email;
    }

    public int getMatricula() {
        return this.matricula;
    }

    public String getNome() {
        return this.nome;
    }

    public String getSenha() {
        return this.senha;
    }

    public TipoUsuario getTipo() {
        return this.type;
    }

    public void delete() {
        Sistema.excluirUsuario(this);
    }
}
