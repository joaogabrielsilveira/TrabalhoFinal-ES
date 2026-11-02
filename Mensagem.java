public class Mensagem {

    @SuppressWarnings("unused")
    private final int id;
    @SuppressWarnings("unused")
    public static int ID = 0;

    private String titulo;
    private String conteudo;
    @SuppressWarnings("unused")
    private Usuario remetente;
    @SuppressWarnings("unused")
    private Usuario destinatario;
    private boolean lida;

    public Mensagem(String titulo, String conteudo, Usuario remetente, Usuario destinatario) {
        this.titulo = titulo;
        this.id = ID;
        ID++;
        this.conteudo = conteudo;
        this.remetente = remetente;
        this.destinatario = destinatario;
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

    public Usuario getRemetente() {
        return remetente;
    }

    public Usuario getDestinatario() {
        return destinatario;
    }

    public int getId() {
        return id;
    }
}