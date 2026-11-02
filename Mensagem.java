public class Mensagem {

    @SuppressWarnings("unused")
    private final int id;
    @SuppressWarnings("unused")
    public static int ID = 0;

    private String conteudo;
    @SuppressWarnings("unused")
    private Usuario remetente;
    @SuppressWarnings("unused")
    private Usuario destinatario;
    private boolean lida;

    public Mensagem(int id, String conteudo, Usuario remetente, Usuario destinatario) {
        this.id = id;
        this.conteudo = conteudo;
        this.remetente = remetente;
        this.destinatario = destinatario;
    }

    public boolean isLida() {
        return lida;
    }

    public void setLida(boolean lida) {
        this.lida = lida;
    }

    public String getConteudo() {
        setLida(true);
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