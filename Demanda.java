import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Demanda {

    public static int ID = 0;

    private final int id;
    private int matriculaSolicitante;
    private String emailSolicitante;
    private List<Tarefa> tarefas;
    private String titulo;
    private String descricao;
    private StatusDemanda situacao;
    private PrioridadeDemanda prioridade;
    @SuppressWarnings("unused")
    private LocalDateTime data_criacao;
    @SuppressWarnings("unused")
    private LocalDateTime encerramento;
    private LocalDateTime prazo;

    public Demanda(Usuario solicitante, String titulo, String descricao, LocalDateTime prazo) {
        this.id = ID;
        ID++;
        this.matriculaSolicitante = solicitante.getMatricula();
        this.emailSolicitante = solicitante.getEmail();
        this.titulo = titulo;
        this.descricao = descricao;
        this.situacao = StatusDemanda.Aberta;
        this.data_criacao = LocalDateTime.now();
        this.prazo = prazo;
        this.tarefas = new ArrayList<>();
    }

    public StatusDemanda getSituacao() {

        return this.situacao;
    }

    public int getId() {
        return this.id;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public void setSituacao(StatusDemanda situacao) {
        this.situacao = situacao;
    }

    public void cancelarDemanda() {


    }

    public int getMatriculaSolicitante() {
        return this.matriculaSolicitante;
    }

    public String getEmailSolicitante() {
        return this.emailSolicitante;
    }

    public List<Tarefa> getTarefas() {
        return this.tarefas;
    }

    public void editarDados(String novoConteudo, LocalDateTime novoPrazo) {
        this.descricao = novoConteudo;
        this.prazo = novoPrazo;        
    }

    public void checarPrazo() {

        System.out.println("O prazo da demanda é: " + this.prazo);
        System.out.println("Dias restantes: %d " + LocalDateTime.now().until(this.prazo, java.time.temporal.ChronoUnit.DAYS));

        if (LocalDateTime.now().isAfter(this.prazo)) {
            System.out.println("A demanda está atrasada.");
        } else {
            System.out.println("A demanda está dentro do prazo.");
        }
    }

    public int getPrazo() {
        return (int) LocalDateTime.now().until(this.prazo, java.time.temporal.ChronoUnit.DAYS);
    }

    public String getTitulo() {
        return this.titulo;
    }

    public void setPrioridade(PrioridadeDemanda prioridade) {
        this.prioridade = prioridade;
    }

    public PrioridadeDemanda getPrioridade() {
        return this.prioridade;
    }

    public void finalizarDemanda() {
        this.situacao = StatusDemanda.Finalizada;
        this.encerramento = LocalDateTime.now();
    }

}