import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Desenvolvedor extends Usuario {

    private List<Integer> idsDemandasAtribuidas;
    // As tarefas existem uma única vez, no Sistema (cada uma aponta para sua demanda).

    public Desenvolvedor(int matricula, String nome, String email, String senha, TipoUsuario tipo) {
        super(matricula, nome, email, senha, tipo);
        this.idsDemandasAtribuidas = new ArrayList<>();
    }

    public Tarefa criarTarefa(String titulo, String descricao, Demanda demandaSelecionada) {
        Tarefa tarefa = new Tarefa(titulo, descricao, demandaSelecionada);
        Sistema.registrarTarefa(tarefa);
        return tarefa;
    }

    public void receberDemanda(Demanda demanda) {
        idsDemandasAtribuidas.add(demanda.getId());
        Sistema.atualizarUsuario(this);
    }

    public List<Demanda> getDemandasAtribuidas() {
        return idsDemandasAtribuidas.stream().map(Sistema::getDemanda).collect(Collectors.toList());
    }

    public List<Tarefa> getTarefasAtribuidas() {
        return Sistema.getTarefas().stream()
            .filter(t -> idsDemandasAtribuidas.contains(t.getDemandaVinculada()))
            .collect(Collectors.toList());
    }

    public void setStatusTarefa(Tarefa tarefa, StatusTarefa status) {
        tarefa.setStatus(status);
        Sistema.salvarTarefas();
    }

    public Demanda entregarDemanda(Demanda demandaEntregue) {
        demandaEntregue.finalizarDemanda();
        Sistema.removerTarefasDaDemanda(demandaEntregue.getId());
        Sistema.salvarDemandas();

        Usuario cliente = Sistema.getUsuario(demandaEntregue.getEmailSolicitante());
        enviarMensagem("Demanda finalizada", String.format("Sua demanda (%s) [id %d] foi finalizada pelo desenvolvedor.", demandaEntregue.getTitulo(), demandaEntregue.getId()), cliente);
        return demandaEntregue;
    }

    public void atualizarTarefa(Tarefa tarefaSelecionada, String novoConteudo) {
        tarefaSelecionada.atualizarTarefa(novoConteudo);
        Sistema.salvarTarefas();
    }

    public void finalizarTarefa(Tarefa t) {
        t.setStatus(StatusTarefa.Concluida);
        Sistema.salvarTarefas();
    }
}
