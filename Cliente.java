
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Cliente extends Usuario {
    
    List<Demanda> demandasSolicitadas;

    public Cliente(int matricula, String nome, String email, String senha, TipoUsuario tipo) {
        super(matricula, nome, email, senha, tipo);
        this.demandasSolicitadas = new ArrayList<>();
    }

    public Demanda solicitarDemanda(String titulo, String descricao, int prazo) {
        Demanda novaDemanda = new Demanda(this, titulo, descricao, LocalDateTime.now().plusDays(prazo));
        demandasSolicitadas.add(novaDemanda);
        Sistema.atualizarUsuario(this);

        PriorityQueue<Avaliador> minHeapAvaliadoresDisponiveis = new PriorityQueue<>((d1, d2) -> Integer.compare(d1.getDemandasParaAprovacao().size(), d2.getDemandasParaAprovacao().size()));

        for (Avaliador a : Sistema.getAvaliadores()) {
            minHeapAvaliadoresDisponiveis.offer(a);
        }

        Avaliador avaliadorSelecionado = minHeapAvaliadoresDisponiveis.poll();
        avaliadorSelecionado.receberDemanda(novaDemanda, this);
        Sistema.atualizarUsuario(this);

        return novaDemanda;
    }

    public List<Demanda> getDemandasSolicitadas() {
        return this.demandasSolicitadas;
    }

    public Demanda atualizarDemanda(Demanda demandaSelecionada, String novoConteudo, int novoPrazo) {

        demandaSelecionada.editarDados(novoConteudo, LocalDateTime.now().plusDays(novoPrazo));
        Sistema.atualizarUsuario(this);
        return demandaSelecionada;
    }
}
