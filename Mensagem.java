public class Mensagem {

    @SuppressWarnings("unused")
    private int id;
    @SuppressWarnings("unused")
    private String conteudo;
    @SuppressWarnings("unused")
    private Usuario remetente;
    @SuppressWarnings("unused")
    private Usuario destinatario;

    public Mensagem(int id, String conteudo, Usuario remetente, Usuario destinatario) {
        this.id = id;
        this.conteudo = conteudo;
        this.remetente = remetente;
        this.destinatario = destinatario;
    }

}