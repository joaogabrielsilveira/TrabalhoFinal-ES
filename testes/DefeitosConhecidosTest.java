import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Defeitos conhecidos")
public class DefeitosConhecidosTest {

    private Cliente cliente;
    private Avaliador avaliador;

    @BeforeEach
    void setUp() {
        Sistema sistema = TestHelper.resetarAmbiente();
        Demanda.resetarContador();
        cliente = new Cliente(0, "João", "joao@test.com", "123", TipoUsuario.Cliente);
        avaliador = new Avaliador(1, "Pedro", "pedro@test.com", "123", TipoUsuario.Avaliador);
        sistema.registrarUsuario(cliente);
        sistema.registrarUsuario(avaliador);
    }

    @Test
    @Disabled("Demanda.getPrazo() usa ChronoUnit.DAYS a partir de LocalDateTime.now(). " +
              "Como alguns milissegundos se passam entre a criação e a leitura, uma demanda criada com " +
              "15 dias (fronteira Média) já retorna 14 dias e é classificada erroneamente como Alta.")
    @DisplayName("BUG: Demanda criada na fronteira de 15 dias não deve perder 1 dia por truncamento de milissegundos")
    void bugTruncamentoDeDiasNaFronteiraDePrioridade() {
        Demanda d15 = cliente.solicitarDemanda("Fronteira 15 dias", "Deve ter prioridade Média", 15);
        Demanda d31 = cliente.solicitarDemanda("Fronteira 31 dias", "Deve ter prioridade Baixa", 31);

        assertEquals(15, d15.getPrazo(), "Prazo recém-criado de 15 dias foi truncado para 14");
        assertEquals(31, d31.getPrazo(), "Prazo recém-criado de 31 dias foi truncado para 30");

        avaliador.atualizarPrioridade(d15);
        avaliador.atualizarPrioridade(d31);

        assertEquals(PrioridadeDemanda.Média, d15.getPrioridade());
        assertEquals(PrioridadeDemanda.Baixa, d31.getPrioridade());
    }
}