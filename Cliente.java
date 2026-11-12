import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Cliente extends Usuario {

    // Só os IDs: a Demanda real vive uma única vez no Sistema.
    private List<Integer> idsDemandasSolicitadas;

    public Cliente(int matricula, String nome, String email, String senha, TipoUsuario tipo) {
        super(matricula, nome, email, senha, tipo);
        this.idsDemandasSolicitadas = new ArrayList<>();
    }

    public Demanda solicitarDemanda(String titulo, String descricao, int prazo) {
        Demanda novaDemanda = new Demanda(this, titulo, descricao, LocalDateTime.now().plusDays(prazo));
        novaDemanda.setSituacao(StatusDemanda.Aberta);
        idsDemandasSolicitadas.add(novaDemanda.getId());
        Sistema.registrarDemanda(novaDemanda);
        Sistema.atualizarUsuario(this);

        // Avaliador com menos demandas pendentes
        Avaliador avaliadorSelecionado = Sistema.getAvaliadores().stream()
            .min(Comparator.comparingInt(a -> a.getDemandasParaAprovacao().size()))
            .orElse(null);

        if (avaliadorSelecionado == null) {
            System.out.println("Nenhum avaliador cadastrado: a demanda ficará aberta até existir um.");
        } else {
            avaliadorSelecionado.receberDemanda(novaDemanda, this);
        }

        return novaDemanda;
    }

    public List<Demanda> getDemandasSolicitadas() {
        return idsDemandasSolicitadas.stream()
            .map(Sistema::getDemanda)
            .collect(Collectors.toList());
    }

    public Demanda atualizarDemanda(Demanda demandaSelecionada, String novoConteudo, int novoPrazo) {
        demandaSelecionada.editarDados(novoConteudo, LocalDateTime.now().plusDays(novoPrazo));
        Sistema.salvarDemandas();
        return demandaSelecionada;
    }
}
