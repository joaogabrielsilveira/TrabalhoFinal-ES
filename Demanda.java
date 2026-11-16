import java.time.LocalDateTime;
import java.util.List;


public class Demanda {

    public static int ID = 0;

    private final int id;
    private int matriculaSolicitante;
    private String emailSolicitante;
    private String titulo;
    private String descricao;
    private StatusDemanda situacao;
    private PrioridadeDemanda prioridade;
    @SuppressWarnings("unused")
    private LocalDateTime dataCriacao;
    @SuppressWarnings("unused")
    private LocalDateTime encerramento;
    private LocalDateTime prazo;
    private String emailAvaliadorAlocado;
    private String emailDesenvolvedorAlocado;

    public Demanda(Usuario solicitante, String titulo, String descricao, LocalDateTime prazo) {
        this.id = ID;
        ID++;
        this.matriculaSolicitante = solicitante.getMatricula();
        this.emailSolicitante = solicitante.getEmail();
        this.titulo = titulo;
        this.descricao = descricao;
        this.situacao = StatusDemanda.Aberta;
        this.dataCriacao = LocalDateTime.now();
        this.prazo = prazo;
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

    /**
     * Cancelamento definitivo (feito pelo avaliador). Só vale para demandas que ainda não
     * foram aprovadas (Aberta ou Retornada). Depois disso a demanda não pode ser editada,
     * reenviada nem reaberta: o solicitante só pode visualizá-la.
     */
    public void cancelarDemanda() {
        if (this.situacao != StatusDemanda.Aberta && this.situacao != StatusDemanda.Retornada) {
            throw new IllegalStateException("Demanda na situação " + this.situacao + " não pode ser cancelada.");
        }
        this.situacao = StatusDemanda.Cancelada;
        this.encerramento = LocalDateTime.now();
    }

    public int getMatriculaSolicitante() {
        return this.matriculaSolicitante;
    }

    public String getEmailSolicitante() {
        return this.emailSolicitante;
    }

    public List<Tarefa> getTarefas() {
        // Derivado do Sistema: as tarefas existem uma única vez lá.
        return Sistema.getTarefasDaDemanda(this.id);
    }

    /** Só demandas Abertas ou Retornadas podem ser editadas pelo solicitante. */
    public boolean podeSerEditada() {
        return this.situacao == StatusDemanda.Aberta || this.situacao == StatusDemanda.Retornada;
    }

    public void editarDados(String novoConteudo, LocalDateTime novoPrazo) {
        if (!podeSerEditada()) {
            throw new IllegalStateException("Demanda na situação " + this.situacao + " não pode ser editada.");
        }
        this.descricao = novoConteudo;
        this.prazo = novoPrazo;
    }

    public void checarPrazo() {

        System.out.println("O prazo da demanda é: " + this.prazo);
        System.out.println("Dias restantes: " + LocalDateTime.now().until(this.prazo, java.time.temporal.ChronoUnit.DAYS));

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

    public String getEmailAvaliador() {
        return this.emailAvaliadorAlocado;
    }

    public String getEmailDesenvolvedor() {
        return this.emailDesenvolvedorAlocado;
    }

    public void setEmailAvaliador(String email) {
        this.emailAvaliadorAlocado = email;
    }

    public void setEmailDesenvolvedor(String email) {
        this.emailDesenvolvedorAlocado = email;
    }

    public void finalizarDemanda() {
        this.situacao = StatusDemanda.Finalizada;
        this.encerramento = LocalDateTime.now();
    }

    public static void resetarContador() {
        ID = 0;
    }
}