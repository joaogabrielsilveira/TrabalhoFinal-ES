public class Tarefa {

    public static int ID = 0;
    private final int id;
    private String titulo;
    private String descricao;
    private Demanda demandaVinculada;
    private StatusTarefa status;


    public Tarefa(String titulo, String descricao, Demanda demandaVinculada) {
        this.id = ++ID;
        this.titulo = titulo;
        this.descricao = descricao;
        this.demandaVinculada = demandaVinculada;
        this.status = StatusTarefa.EmAndamento;
    }

    public int getId() {
        return id;
    }

    public StatusTarefa getStatus() {
        return status;
    }

    public void setStatus(StatusTarefa status) {
        this.status = status;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public Demanda getDemandaVinculada() {
        return demandaVinculada;
    }

    public void atualizarTarefa(String novaDescricao) {
        this.descricao = novaDescricao;
    }
}