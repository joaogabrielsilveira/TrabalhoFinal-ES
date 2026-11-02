import java.time.LocalDateTime;

public class Mensagem {
    private final int id;
    public static int ID = 0;

    private String titulo;
    private String conteudo;
    private String nomeRemetente;
    private String emailRemetente;
    private String nomeDestinatario;
    private String emailDestinatario;
    private LocalDateTime horarioEnvio;
    private boolean lida;

    public Mensagem(String titulo, String conteudo, Usuario remetente, Usuario destinatario) {
        this.titulo = titulo;
        this.id = ID;
        ID++;
        this.conteudo = conteudo;
        this.nomeRemetente = remetente.getNome();
        this.emailRemetente = remetente.getEmail();
        this.nomeDestinatario = destinatario.getNome();
        this.emailDestinatario = destinatario.getNome();
        this.horarioEnvio = LocalDateTime.now();
    }

    public boolean isLida() {
        return lida;
    }

    public void setLida() {
        this.lida = true;
    }

    public String getConteudo() {
        setLida();
        return conteudo;
    }

    public String getTitulo() {
        return this.titulo;
    }

    public String getNomeRemetente() {
        return this.nomeRemetente;
    }

    public String getEmailRemetente() {
        return this.emailRemetente;
    }

    public String getNomeDestinatario() {
        return this.nomeDestinatario;
    }

    public String getEmailDestinatario() {
        return this.emailDestinatario;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getData() {
        return this.horarioEnvio;
    }
}