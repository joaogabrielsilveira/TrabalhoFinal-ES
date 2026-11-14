import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Cliente extends Usuario {

    // Só os IDs: a Demanda real vive uma única vez no Sistema.
    private List<Integer> idsDemandasSolicitadas;

    public Cliente(int matricula, String nome, String email, String senha, TipoUsuario tipo) {
        super(matricula, nome, email, senha, tipo);
        this.idsDemandasSolicitadas = new ArrayList<>();
    }

    /** O Gson não chama construtores: listas ausentes no JSON ficariam null. */
    @Override
    public void aposCarregar() {
        if (idsDemandasSolicitadas == null) idsDemandasSolicitadas = new ArrayList<>();
    }

    public Demanda solicitarDemanda(String titulo, String descricao, int prazo) {
        if (prazo <= 0) {
            throw new IllegalArgumentException("O prazo deve ser de pelo menos 1 dia.");
        }
        Demanda novaDemanda = new Demanda(this, titulo, descricao, LocalDateTime.now().plusDays(prazo));
        novaDemanda.setSituacao(StatusDemanda.Aberta);
        idsDemandasSolicitadas.add(novaDemanda.getId());
        Sistema.registrarDemanda(novaDemanda);
        Sistema.atualizarUsuario(this);

        encaminharParaAvaliador(novaDemanda, false);
        return novaDemanda;
    }

    public List<Demanda> getDemandasSolicitadas() {
        return idsDemandasSolicitadas.stream()
            .map(Sistema::getDemanda)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Edita uma demanda do próprio cliente.
     *  - Aberta: edição livre; a demanda continua na fila do avaliador.
     *  - Retornada: edição permitida; depois dela a demanda volta a ficar Aberta e é
     *    reencaminhada automaticamente ao MESMO avaliador que a retornou.
     *  - Cancelada, EmAndamento ou Finalizada: não pode ser editada (IllegalStateException).
     *    Uma demanda Cancelada só pode ser visualizada pelo solicitante.
     */
    public Demanda atualizarDemanda(Demanda demandaSelecionada, String novoConteudo, int novoPrazo) {
        if (demandaSelecionada == null || !getEmail().equals(demandaSelecionada.getEmailSolicitante())) {
            throw new IllegalArgumentException("Só o solicitante pode editar a demanda.");
        }
        if (demandaSelecionada.getSituacao() == StatusDemanda.Cancelada) {
            throw new IllegalStateException("Demanda cancelada pelo avaliador: não pode ser editada nem reenviada.");
        }
        if (novoPrazo <= 0) {
            throw new IllegalArgumentException("O prazo deve ser de pelo menos 1 dia.");
        }

        boolean estavaRetornada = demandaSelecionada.getSituacao() == StatusDemanda.Retornada;

        // Lança IllegalStateException se a situação não permitir edição (nada é alterado nesse caso)
        demandaSelecionada.editarDados(novoConteudo, LocalDateTime.now().plusDays(novoPrazo));

        if (estavaRetornada) {
            demandaSelecionada.setSituacao(StatusDemanda.Aberta);
            Sistema.salvarDemandas();
            encaminharParaAvaliador(demandaSelecionada, true);
        } else {
            Sistema.salvarDemandas();
        }
        return demandaSelecionada;
    }

    /**
     * Coloca a demanda na fila de um avaliador.
     * @param preferirAnterior true = tenta o avaliador já associado à demanda (o que a retornou);
     *                         se ele não existir mais, cai no avaliador menos carregado.
     */
    private void encaminharParaAvaliador(Demanda d, boolean preferirAnterior) {
        if (d.getSituacao() == StatusDemanda.Cancelada) {
            return; // demanda cancelada nunca volta para a fila de nenhum avaliador
        }
        Avaliador alvo = null;

        if (preferirAnterior && Sistema.getUsuario(d.getEmailAvaliador()) instanceof Avaliador anterior) {
            alvo = anterior;
        }
        if (alvo == null) {
            alvo = Sistema.getAvaliadores().stream()
                .min(Comparator.comparingInt(a -> a.getDemandasParaAprovacao().size()))
                .orElse(null);
        }

        if (alvo == null) {
            System.out.println("Nenhum avaliador cadastrado: a demanda ficará aberta até existir um.");
        } else {
            alvo.receberDemanda(d, this);
        }
    }
}
