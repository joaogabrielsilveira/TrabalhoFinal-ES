
import java.util.ArrayList;
import java.util.List;

public class Desenvolvedor extends Usuario {
    
    List<Demanda> demandasAtribuidas;
    List<Tarefa> tarefasAtribuidas;

    public Desenvolvedor(int matricula, String nome, String email, String senha, TipoUsuario tipo) {
        super(matricula, nome, email, senha, tipo);
        this.demandasAtribuidas = new ArrayList<>();
        this.tarefasAtribuidas = new ArrayList<>();
    }
    public Tarefa criarTarefa(String titulo, String descricao, Demanda demandaSelecionada) {
        Tarefa tarefa = new Tarefa(titulo, descricao, demandaSelecionada);
        demandaSelecionada.getTarefas().add(tarefa);
        tarefasAtribuidas.add(tarefa);
        Sistema.atualizarUsuario(this);
        return tarefa;
    }

    public void receberDemanda(Demanda demanda) {
        demandasAtribuidas.add(demanda);
        Sistema.atualizarUsuario(this);
    }

    public List<Demanda> getDemandasAtribuidas() {
        return demandasAtribuidas;
    }

    public List<Tarefa> getTarefasAtribuidas() {
        return this.tarefasAtribuidas;
    }
    
    public void setStatusTarefa(Tarefa tarefa, StatusTarefa status) {
        tarefa.setStatus(status);
    }

    public Demanda entregarDemanda(Demanda demandaEntregue) {
        demandaEntregue.setSituacao(StatusDemanda.Finalizada);
        demandaEntregue.getTarefas().clear();
        Usuario cliente = Sistema.getUsuario(demandaEntregue.getEmailSolicitante());
        enviarMensagem("Demanda finalizada", String.format("Sua demanda (%s) [id %d] foi finalizada pelo desenvolvedor.", demandaEntregue.getTitulo(), demandaEntregue.getId()), cliente);
        Sistema.atualizarUsuario(this);
        return demandaEntregue;
    }

    public void atualizarTarefa(Tarefa tarefaSelecionada, String novoConteudo) {
        tarefaSelecionada.atualizarTarefa(novoConteudo);
    }
}
