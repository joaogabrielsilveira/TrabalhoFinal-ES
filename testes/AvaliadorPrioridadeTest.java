import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Regras do avaliador")
public class AvaliadorPrioridadeTest {

    private Avaliador avaliador;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        TestHelper.resetarAmbiente();
        Demanda.resetarContador();
        avaliador = new Avaliador(0, "Pedro", "pedro@test.com", "123", TipoUsuario.Avaliador);
        cliente = new Cliente(1, "João", "joao@test.com", "123", TipoUsuario.Cliente);
    }

    @Test
    @DisplayName("Deve classificar prioridade automaticamente conforme o prazo e valores de fronteira (14/15 e 30/31)")
    void deveCalcularPrioridadePeloPrazo() {
        // Contorno (+1 hora) para evitar o truncamento de milissegundos em Demanda.getPrazo()
        Demanda fronteiraAlta = new Demanda(cliente, "D14", "Desc", LocalDateTime.now().plusDays(14).plusHours(1));
        Demanda fronteiraMediaInf = new Demanda(cliente, "D15", "Desc", LocalDateTime.now().plusDays(15).plusHours(1));
        Demanda fronteiraMediaSup = new Demanda(cliente, "D30", "Desc", LocalDateTime.now().plusDays(30).plusHours(1));
        Demanda fronteiraBaixa = new Demanda(cliente, "D31", "Desc", LocalDateTime.now().plusDays(31).plusHours(1));

        avaliador.atualizarPrioridade(fronteiraAlta);
        avaliador.atualizarPrioridade(fronteiraMediaInf);
        avaliador.atualizarPrioridade(fronteiraMediaSup);
        avaliador.atualizarPrioridade(fronteiraBaixa);

        assertEquals(PrioridadeDemanda.Alta, fronteiraAlta.getPrioridade());
        assertEquals(PrioridadeDemanda.Média, fronteiraMediaInf.getPrioridade());
        assertEquals(PrioridadeDemanda.Média, fronteiraMediaSup.getPrioridade());
        assertEquals(PrioridadeDemanda.Baixa, fronteiraBaixa.getPrioridade());
    }

    @Test
    @DisplayName("Deve lançar exceção se avaliador tentar agir sobre demanda fora de sua fila")
    void deveImpedirAcaoEmDemandaForaDaFila() {
        Demanda d = new Demanda(cliente, "D1", "Desc", LocalDateTime.now().plusDays(10));

        assertThrows(IllegalStateException.class, () -> avaliador.encaminharDemanda(d));
        assertThrows(IllegalStateException.class, () -> avaliador.retornarDemanda(d, "Incompleto"));
        assertThrows(IllegalStateException.class, () -> avaliador.cancelarDemanda(d, "Inválido"));
    }
}