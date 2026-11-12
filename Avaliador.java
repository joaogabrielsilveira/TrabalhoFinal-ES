import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Avaliador extends Usuario {

    private List<Integer> idsParaAprovacao;
    private List<Integer> idsAprovadas;

    public Avaliador(int matricula, String nome, String email, String senha, TipoUsuario tipo) {
        super(matricula, nome, email, senha, tipo);
        this.idsParaAprovacao = new ArrayList<>();
        this.idsAprovadas = new ArrayList<>();
    }

    public List<Demanda> getDemandasParaAprovacao() {
        return idsParaAprovacao.stream().map(Sistema::getDemanda).collect(Collectors.toList());
    }

    public List<Demanda> getDemandasAprovadas() {
        return idsAprovadas.stream().map(Sistema::getDemanda).collect(Collectors.toList());
    }

    public void receberDemanda(Demanda d, Usuario solicitante) {
        idsParaAprovacao.add(d.getId());
        d.setEmailAvaliador(this.getEmail());
        Sistema.salvarDemandas();
        Sistema.atualizarUsuario(this);
        enviarMensagem("Demanda encaminhada", String.format("Sua demanda (%s) [id %d] foi encaminhada para o avaliador: ", d.getTitulo(), d.getId()) + this.getNome(), solicitante);
    }

    public void atualizarPrioridade(Demanda demanda) {
        int prazo = demanda.getPrazo();
        if (prazo <= 14) {
            demanda.setPrioridade(PrioridadeDemanda.Alta);
        } else if (prazo <= 30) {
            demanda.setPrioridade(PrioridadeDemanda.Média);
        } else {
            demanda.setPrioridade(PrioridadeDemanda.Baixa);
        }
        Sistema.salvarDemandas();
    }

    public void setarPrioridade(Demanda demanda, PrioridadeDemanda prioridade) {
        demanda.setPrioridade(prioridade);
        Sistema.salvarDemandas();
    }

    public void retornarDemanda(Demanda demandaRetorno, String motivoRetorno) {
        demandaRetorno.setSituacao(StatusDemanda.Retornada);
        idsParaAprovacao.remove(Integer.valueOf(demandaRetorno.getId()));
        Sistema.salvarDemandas();
        Sistema.atualizarUsuario(this);

        Usuario solicitante = Sistema.getUsuario(demandaRetorno.getEmailSolicitante());
        enviarMensagem("Demanda retornada", "Sua demanda foi retornada para ajustes. Motivo: " + motivoRetorno, solicitante);
    }

    /** Devolve o desenvolvedor escolhido, ou null se não houver nenhum cadastrado. */
    public Desenvolvedor encaminharDemanda(Demanda demandaEncaminhada) {

        Desenvolvedor desenvolvedorSelecionado = Sistema.getDesenvolvedores().stream()
            .min(Comparator.comparingInt(d -> d.getDemandasAtribuidas().size()))
            .orElse(null);

        if (desenvolvedorSelecionado == null) {
            return null;
        }

        demandaEncaminhada.setSituacao(StatusDemanda.EmAndamento);
        demandaEncaminhada.setEmailDesenvolvedor(desenvolvedorSelecionado.getEmail());
        desenvolvedorSelecionado.receberDemanda(demandaEncaminhada);

        idsAprovadas.add(demandaEncaminhada.getId());
        idsParaAprovacao.remove(Integer.valueOf(demandaEncaminhada.getId()));
        Sistema.salvarDemandas();
        Sistema.atualizarUsuario(this);

        Usuario solicitante = Sistema.getUsuario(demandaEncaminhada.getEmailSolicitante());
        enviarMensagem("Demanda encaminhada", String.format("Sua demanda (%s) [id %d] foi encaminhada para o desenvolvedor: ", demandaEncaminhada.getTitulo(), demandaEncaminhada.getId()) + desenvolvedorSelecionado.getNome(), solicitante);
        return desenvolvedorSelecionado;
    }
}
