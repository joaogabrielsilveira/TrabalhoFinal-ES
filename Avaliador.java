
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Avaliador extends Usuario {
    
    List<Demanda> demandasParaAprovacao;
    List<Demanda> demandasAprovadas;

    public List<Demanda> getDemandasParaAprovacao() {
        return this.demandasParaAprovacao;
    }

    public Avaliador(int matricula, String nome, String email, String senha, TipoUsuario tipo) {
        super(matricula, nome, email, senha, tipo);
        this.demandasParaAprovacao = new ArrayList<>();
        this.demandasAprovadas = new ArrayList<>();
    }

    public void receberDemanda(Demanda d, Usuario solicitante) {
        demandasParaAprovacao.add(d);
        enviarMensagem("Demanda encaminhada", String.format("Sua demanda (%s) [id %d] foi encaminhada para o avaliador: ", d.getTitulo(), d.getId()) + this.getNome(), solicitante);
        Sistema.atualizarUsuario(this);

    }

    public void atualizarPrioridade(Demanda demanda) {
        if (demanda.getPrazo() <= 14) {
            demanda.setPrioridade(PrioridadeDemanda.Alta);
        } else if (demanda.getPrazo() > 14 && demanda.getPrazo() <= 30) {
            demanda.setPrioridade(PrioridadeDemanda.Média);
        } else if (demanda.getPrazo() > 30) {
            demanda.setPrioridade(PrioridadeDemanda.Baixa);
        }
    }

    public void setarPrioridade(Demanda demanda, PrioridadeDemanda prioridade) {
        demanda.setPrioridade(prioridade);
    }

    public void retornar_demanda(Demanda demandaRetorno, String motivoRetorno) {
        demandaRetorno.setSituacao(StatusDemanda.Retornada);
        enviarMensagem("Demanda retornada", "Sua demanda foi retornada para ajustes. Motivo: " + motivoRetorno, this);
        Sistema.atualizarUsuario(this);
        demandasParaAprovacao.remove(demandaRetorno);
    }
    
    public Desenvolvedor encaminharDemanda(Demanda demandaEncaminhada) {

        PriorityQueue<Desenvolvedor> minHeapDesenvolvedoresDisponiveis = new PriorityQueue<>((d1, d2) -> Integer.compare(d1.getDemandasAtribuidas().size(), d2.getDemandasAtribuidas().size()));

        for (Desenvolvedor desenvolvedor : Sistema.getDesenvolvedores()) {
            minHeapDesenvolvedoresDisponiveis.offer(desenvolvedor);
        }

        Desenvolvedor desenvolvedorSelecionado = minHeapDesenvolvedoresDisponiveis.poll();
        desenvolvedorSelecionado.receberDemanda(demandaEncaminhada);
        enviarMensagem("Demanda encaminhada", String.format("Sua demanda (%s) [id %d] foi encaminhada para o desenvolvedor: ", demandaEncaminhada.getTitulo(), demandaEncaminhada.getId()) + desenvolvedorSelecionado.getNome(), this);
        demandasParaAprovacao.remove(demandaEncaminhada);
        Sistema.atualizarUsuario(this);
        return desenvolvedorSelecionado;
    }
}