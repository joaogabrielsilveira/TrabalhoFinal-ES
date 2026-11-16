import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Simulação de interface CLI (System.in / System.out)")
public class InterfaceConsoleE2ETest {

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream saidaCapturada;

    @BeforeEach
    void setUp() {
        TestHelper.resetarAmbiente();
        Demanda.resetarContador();
        saidaCapturada = new ByteArrayOutputStream();
        System.setOut(new PrintStream(saidaCapturada, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
        Sistema.sc = new Scanner(System.in);
    }

    private void simularEntradasDoTeclado(String... linhas) {
        String script = String.join(System.lineSeparator(), linhas) + System.lineSeparator();
        ByteArrayInputStream in = new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8));
        System.setIn(in);
        Sistema.sc = new Scanner(in, StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Deve cadastrar Cliente, Avaliador e Dev pelo menu, criar demanda, aprovar e entregar via console")
    void deveExecutarFluxoCompletoPeloMenuInterativo() {
        simularEntradasDoTeclado(
            // 1. Registrar Avaliador
            "2", "Pedro Avaliador", "pedro@cli.com", "123", "2",
            // 2. Registrar Desenvolvedor
            "2", "Tiago Dev", "tiago@cli.com", "123", "3",
            // 3. Registrar Cliente
            "2", "Joao Cliente", "joao@cli.com", "123", "1",

            // 4. Login como Cliente -> Criar Demanda -> Logout
            "1", "joao@cli.com", "123",
            "1", "Sistema de Ponto", "Registrar ponto eletrônico", "15",
            "4",

            // 5. Login como Avaliador -> Ver demandas -> Selecionar 1ª -> Aprovar (fila esvazia e sai sozinha) -> Logout
            "1", "pedro@cli.com", "123",
            "1", "1", "1",
            "3",

            // 6. Login como Desenvolvedor -> Ver demandas -> Selecionar 1ª -> Entregar -> SIM -> Voltar (lista mantém finalizada) -> Logout
            "1", "tiago@cli.com", "123",
            "1", "1", "3", "SIM", "2",
            "4",

            // 7. Sair do Sistema
            "4"
        );

        Sistema sistema = new Sistema();
        sistema.loopSistema();

        String console = saidaCapturada.toString(StandardCharsets.UTF_8);

        assertTrue(console.contains("Conta registrada com sucesso para o usuário Pedro Avaliador"));
        assertTrue(console.contains("Conta registrada com sucesso para o usuário Tiago Dev"));
        assertTrue(console.contains("Conta registrada com sucesso para o usuário Joao Cliente"));
        assertTrue(console.contains("Demanda criada com sucesso, ID: 0"));
        assertTrue(console.contains("Demanda encaminhada para o desenvolvedor: Tiago Dev"));
        assertTrue(console.contains("Demanda entregue com sucesso."));
        assertTrue(console.contains("Saindo do sistema"));

        Demanda d = Sistema.getDemanda(0);
        assertNotNull(d);
        assertEquals(StatusDemanda.Finalizada, d.getSituacao());
    }
}
