import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


import java.util.ArrayList;
import java.util.List;

public class Usuario {

    private static int ID = 0;
    private final int matricula;
    private String nome;
    private String email;
    private String senha;
    private TipoUsuario type;
    private List<Mensagem> mensagensRecebidas = new ArrayList<>();
    private List<Mensagem> mensagensEnviadas = new ArrayList<>();

    public Usuario(int matricula, String nome, String email, String senha, TipoUsuario tipo) {
        this.matricula = ID;
        ID++;
        this.nome = nome;
        this.email = email;
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        this.senha = encoder.encode(senha);
        this.type = tipo;
    }

    public boolean equals(Usuario outro) {
        return this.matricula == outro.matricula;
    }

    public void enviarMensagem(String titulo, String conteudo, Usuario destinatario) {

        Mensagem novaMensagem = new Mensagem(titulo, conteudo, this, destinatario);

        Banco.armazenarMensagem(novaMensagem);
        this.mensagensEnviadas.addFirst(novaMensagem);
        destinatario.mensagensRecebidas.addFirst(novaMensagem);
        Sistema.atualizarUsuario(this);
        Sistema.atualizarUsuario(destinatario);
    }

    public List<Mensagem> getMensagensRecebidas() {
        return this.mensagensRecebidas;
    }

    public List<Mensagem> getMensagensEnviadas() {
        return this.mensagensEnviadas;
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
        Banco.excluirUsuario(this);
    }
}