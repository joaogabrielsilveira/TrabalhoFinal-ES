import java.time.LocalDateTime;


public class Demanda {
    private String titulo;
    @SuppressWarnings("unused")
    private String descricao;
    @SuppressWarnings("unused")
    private String situacao;
    @SuppressWarnings("unused")
    private String prioridade;
    @SuppressWarnings("unused")
    private LocalDateTime data_criacao;
    @SuppressWarnings("unused")
    private LocalDateTime encerramento;
    @SuppressWarnings("unused")
    private LocalDateTime prazo;

    public Demanda(String titulo, String descricao, String situacao, String prioridade, LocalDateTime data_criacao, LocalDateTime encerramento, LocalDateTime prazo) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.situacao = situacao;
        this.prioridade = prioridade;
        this.data_criacao = data_criacao;
        this.encerramento = encerramento;
        this.prazo = prazo;
    }

    public String get_situacao() {

        return "a";
    }

    public void cancelar_demanda() {


    }

    public void editar_dados() {

        
    }

    public String get_titulo() {
        return this.titulo;
    }




}