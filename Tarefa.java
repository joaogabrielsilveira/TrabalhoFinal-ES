public class Tarefa {

    public static int ID = 0;
    private final int id;
    private String titulo;
    private String descricao;
    private int idDemandaVinculada;
    private StatusTarefa status;


    public Tarefa(String titulo, String descricao, Demanda demandaVinculada) {
        this.id = ++ID;
        this.titulo = titulo;
        this.descricao = descricao;
        this.idDemandaVinculada = demandaVinculada.getId();
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

    public int getDemandaVinculada() {
        return idDemandaVinculada;
    }

    public void atualizarTarefa(String novaDescricao) {
        this.descricao = novaDescricao;
    }

    @Override 
    public String toString() {
        String txt = String.format("[%s] %s - Demanda %d", this.status, this.titulo, this.idDemandaVinculada);
        txt += String.format("\n%s", descricao);
        return txt;
    } 
}