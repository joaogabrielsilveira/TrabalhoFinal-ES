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

@DisplayName("Fluxos avançados de console e persistência entre sessões")
public class ConsoleFluxosAvancadosE2ETest {

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

    private void simularTeclado(String... linhas) {
        String script = String.join(System.lineSeparator(), linhas) + System.lineSeparator();
        ByteArrayInputStream in = new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8));
        System.setIn(in);
        Sistema.sc = new Scanner(in, StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Deve validar opções inválidas, campos vazios, cadastro duplicado, login inválido e menu de logs")
    void deveValidarErrosDeEntradaEMenuDeLogsNoConsole() {
        simularTeclado(
            // 1. Opção inválida no menu principal ("99" e texto "abc")
            "99", "abc",

            // 2. Criar conta com nome vazio
            "2", "", "vazio@cli.com",

            // 3. Criar conta com senha vazia
            "2", "Lucas", "lucas@cli.com", "",

            // 4. Criar conta válida (Cliente)
            "2", "Lucas", "lucas@cli.com", "123", "1",

            // 5. Tentar cadastrar mesmo e-mail duplicado
            "2", "Outro Lucas", "LUCAS@cli.com",

            // 6. Tentativa de login com senha incorreta
            "1", "lucas@cli.com", "senhaErrada",

            // 7. Consultar Logs: Todos (1) -> Enter -> Filtrar Nível AVISO (2) -> Enter -> Filtrar Usuário (3) -> Enter -> Filtrar Ação (4) -> Enter -> Voltar (5)
            "3",
            "1", "",
            "2", "AVISO", "",
            "3", "lucas@cli.com", "",
            "4", "LOGIN", "",
            "5",

            // 8. Sair
            "4"
        );

        new Sistema().loopSistema();
        String console = saidaCapturada.toString(StandardCharsets.UTF_8);

        assertAll("Validações de entrada e logs via terminal",
            () -> assertTrue(console.contains("Escolha inválida, tente novamente.")),
            () -> assertTrue(console.contains("Nome e email não podem ser vazios.")),
            () -> assertTrue(console.contains("A senha não pode ser vazia.")),
            () -> assertTrue(console.contains("Usuário com esse email já existe.")),
            () -> assertTrue(console.contains("Usuário ou senha incorretos, tente novamente")),
            () -> assertTrue(console.contains("[AVISO] [lucas@cli.com] [LOGIN]"))
        );
    }

    @Test
    @DisplayName("Deve executar ciclo Retornar -> Editar -> Cancelar e manter persistência entre duas sessões")
    void deveExecutarRetornarEditarCancelarEPersistirEntreSessoes() {
        // SESSÃO 1: Cadastra usuários, abre demanda, avaliador retorna, cliente edita e reenvia, e sistema encerra
        simularTeclado(
            "2", "Avaliador", "av@cli.com", "123", "2",
            "2", "Cliente", "cli@cli.com", "123", "1",

            // Cliente cria demanda ID 0
            "1", "cli@cli.com", "123",
            "1", "Portal Aluno", "Versão inicial", "10",
            "4",

            // Avaliador retorna demanda (opção 2 = Reprovar/Retornar)
            "1", "av@cli.com", "123",
            "1", "1", "2", "Faltou detalhar login",
            "3",

            // Cliente vê demanda retornada, edita (1) e reenvia -> Voltar (2) -> Logout (4)
            "1", "cli@cli.com", "123",
            "2", "1", "1", "Versão detalhada com OAuth", "15", "2",
            "4",

            // Encerra Sessão 1
            "4"
        );
        new Sistema().loopSistema();

        // SESSÃO 2: Nova instância de Sistema lê do disco; avaliador cancela e cliente lê caixa de entrada
        simularTeclado(
            // Avaliador loga -> Ver demandas (1) -> Seleciona 1ª (1) -> Cancelar (3) -> Motivo -> "SIM" -> Logout (3)
            "1", "av@cli.com", "123",
            "1", "1", "3", "Projeto descontinuado", "SIM",
            "3",

            // Cliente loga -> Gerenciar mensagens (3) -> Caixa de entrada (1) -> Ler 1ª msg (1) -> Voltar (2) -> Voltar lista (2) -> Sair msg (4) -> Logout (4)
            "1", "cli@cli.com", "123",
            "3", "1", "1", "2", "5", "4",
            "4",

            // Encerra Sessão 2
            "4"
        );
        new Sistema().loopSistema();

        String console = saidaCapturada.toString(StandardCharsets.UTF_8);
        Demanda d = Sistema.getDemanda(0);

        assertAll("Persistência entre sessões, cancelamento e leitura de mensagens",
            () -> assertNotNull(d),
            () -> assertEquals(StatusDemanda.Cancelada, d.getSituacao()),
            () -> assertTrue(console.contains("Demanda atualizada e reenviada ao avaliador.")),
            () -> assertTrue(console.contains("Demanda cancelada.")),
            () -> assertTrue(console.contains("Projeto descontinuado"))
        );
    }
}