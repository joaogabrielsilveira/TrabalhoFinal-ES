import java.time.LocalDateTime;


public class Demanda {
    private String titulo;
    private String descricao;
    private String situacao;
    private String prioridade;
    private LocalDateTime data_criacao;
    private LocalDateTime encerramento;
    private LocalDateTime prazo;

    public Demanda() {}

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