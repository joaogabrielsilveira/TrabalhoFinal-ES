import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Avaliador extends Usuario {

    private List<Integer> idsParaAprovacao;
    private List<Integer> idsAprovadas;

    public Avaliador(int matricula, String nome, String email, String senha, TipoUsuario tipo) {
        super(matricula, nome, email, senha, tipo);
        this.idsParaAprovacao = new ArrayList<>();
        this.idsAprovadas = new ArrayList<>();
    }

    /** O Gson não chama construtores: listas ausentes no JSON ficariam null. */
    @Override
    public void aposCarregar() {
        if (idsParaAprovacao == null) idsParaAprovacao = new ArrayList<>();
        if (idsAprovadas == null) idsAprovadas = new ArrayList<>();
    }

    public List<Demanda> getDemandasParaAprovacao() {
        return idsParaAprovacao.stream().map(Sistema::getDemanda).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<Demanda> getDemandasAprovadas() {
        return idsAprovadas.stream().map(Sistema::getDemanda).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void receberDemanda(Demanda d, Usuario solicitante) {
        if (d.getSituacao() == StatusDemanda.Cancelada) {
            throw new IllegalStateException("Demanda cancelada não pode ser (re)enviada para avaliação.");
        }
        if (!idsParaAprovacao.contains(d.getId())) {
            idsParaAprovacao.add(d.getId());
        }
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

    /**
     * Retorna a demanda ao solicitante. O email deste avaliador permanece registrado na demanda
     * (emailAvaliadorAlocado), pois é para ele que a demanda volta depois de editada.
     */
    public void retornarDemanda(Demanda demandaRetorno, String motivoRetorno) {
        exigirNaFila(demandaRetorno);

        demandaRetorno.setSituacao(StatusDemanda.Retornada);
        idsParaAprovacao.remove(Integer.valueOf(demandaRetorno.getId()));
        Sistema.salvarDemandas();
        Sistema.atualizarUsuario(this);

        Usuario solicitante = Sistema.getUsuario(demandaRetorno.getEmailSolicitante());
        enviarMensagem("Demanda retornada", String.format("Sua demanda (%s) [id %d] foi retornada para ajustes. Motivo: %s", demandaRetorno.getTitulo(), demandaRetorno.getId(), motivoRetorno), solicitante);
    }

    /**
     * Cancela definitivamente uma demanda da fila deste avaliador. A demanda sai da fila, fica
     * com a situação Cancelada (não pode mais ser editada nem reenviada) e o solicitante é avisado.
     */
    public void cancelarDemanda(Demanda demandaCancelada, String motivoCancelamento) {
        exigirNaFila(demandaCancelada);

        demandaCancelada.cancelarDemanda();
        idsParaAprovacao.remove(Integer.valueOf(demandaCancelada.getId()));
        Sistema.salvarDemandas();
        Sistema.atualizarUsuario(this);

        Usuario solicitante = Sistema.getUsuario(demandaCancelada.getEmailSolicitante());
        enviarMensagem("Demanda cancelada", String.format("Sua demanda (%s) [id %d] foi cancelada pelo avaliador %s. Motivo: %s", demandaCancelada.getTitulo(), demandaCancelada.getId(), this.getNome(), motivoCancelamento), solicitante);
    }

    /** Devolve o desenvolvedor escolhido, ou null se não houver nenhum cadastrado. */
    public Desenvolvedor encaminharDemanda(Demanda demandaEncaminhada) {
        exigirNaFila(demandaEncaminhada);

        // Desenvolvedor com menos demandas EM ANDAMENTO (finalizadas não contam como carga)
        Desenvolvedor desenvolvedorSelecionado = Sistema.getDesenvolvedores().stream()
            .min(Comparator.comparingInt(Desenvolvedor::getCargaAtual))
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

    /** Só é possível aprovar/retornar demandas que estão na fila DESTE avaliador. */
    private void exigirNaFila(Demanda d) {
        if (d == null || !idsParaAprovacao.contains(d.getId())) {
            throw new IllegalStateException("A demanda não está na fila de aprovação deste avaliador.");
        }
    }
}
