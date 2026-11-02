
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
    public Tarefa criarTarefa() {
        System.out.println("Digite o título da tarefa: ");
        String titulo = Sistema.sc.nextLine();
        System.out.println("Digite a descrição da tarefa: ");
        String descricao = Sistema.sc.nextLine();
        
        for (Demanda demanda : demandasAtribuidas) {
            System.out.println("ID: " + demanda.getId() + ", Título: " + demanda.getTitulo());
        }
        
        System.out.println("Digite a demanda que deseja vincular a tarefa: ");
        final int idDemanda = Sistema.sc.nextInt();
        Demanda demandaSelecionada = demandasAtribuidas.stream().filter(d -> d.getId() == idDemanda).findFirst().orElse(null);
        
        while (demandaSelecionada == null) {
            System.out.println("Demanda inválida. Digite novamente: ");
            final int novoIdDemanda = Sistema.sc.nextInt();
            demandaSelecionada = demandasAtribuidas.stream().filter(d -> d.getId() == novoIdDemanda).findFirst().orElse(null);
        }
        Tarefa tarefa = new Tarefa(titulo, descricao, demandaSelecionada);
        demandaSelecionada.getTarefas().add(tarefa);
        tarefasAtribuidas.add(tarefa);
        return tarefa;
    }

    public void receberDemanda(Demanda demanda) {
        demandasAtribuidas.add(demanda);
    }

    public List<Demanda> getDemandasAtribuidas() {
        return demandasAtribuidas;
    }
    
    public void setStatusTarefa(Tarefa tarefa, StatusTarefa status) {
        tarefa.setStatus(status);
    }

    public Demanda entregarDemanda(Demanda demandaEntregue) {
        
        demandaEntregue.setSituacao(StatusDemanda.Finalizada);
        demandaEntregue.getTarefas().clear();
        enviarMensagem("Demanda finalizada", "Sua demanda foi finalizada pelo desenvolvedor.", demandaEntregue.getSolicitante());

        return demandaEntregue;
    }

    public void atualizarTarefa() {
        System.out.println("As tarefas atribuídas são: ");
        for (Tarefa tarefa : tarefasAtribuidas) {
            System.out.println("Título: " + tarefa.getTitulo() + ", Status: " +
            tarefa.getStatus());
        }
        System.out.println("Digite o ID da tarefa que deseja atualizar: ");
        int idTarefa = Sistema.sc.nextInt();
        Tarefa tarefaSelecionada = tarefasAtribuidas.stream().filter(t -> t.getId() == idTarefa).findFirst().orElse(null);
        System.out.println("Digite o novo conteúdo da tarefa: ");
        String novoConteudo = Sistema.sc.nextLine();
        tarefaSelecionada.atualizarTarefa(novoConteudo);
    }
}
