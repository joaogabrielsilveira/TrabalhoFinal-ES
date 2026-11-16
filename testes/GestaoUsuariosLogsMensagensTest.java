import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Usuários, consultas de log e ordem de mensagens")
public class GestaoUsuariosLogsMensagensTest {

    private Sistema sistema;

    @BeforeEach
    void setUp() {
        sistema = TestHelper.resetarAmbiente();
        Demanda.resetarContador();
    }

    @Test
    @DisplayName("Deve bloquear e-mail duplicado ignorando maiúsculas/espaços, listar por tipo e excluir usuário")
    void deveGerenciarCicloDeVidaDeUsuariosEListagens() {
        Cliente cli1 = new Cliente(0, "Lucas", "lucas@ufmg.br", "123", TipoUsuario.Cliente);
        Cliente cliDup = new Cliente(1, "Lucas Clone", "  LUCAS@UFMG.BR ", "456", TipoUsuario.Cliente);
        Avaliador av = new Avaliador(2, "Michele", "michele@ufmg.br", "123", TipoUsuario.Avaliador);
        Desenvolvedor dev = new Desenvolvedor(3, "Guilherme", "gui@ufmg.br", "123", TipoUsuario.Desenvolvedor);

        assertTrue(sistema.registrarUsuario(cli1));
        assertFalse(sistema.registrarUsuario(cliDup), "Não deve permitir cadastro com e-mail igual em caixa alta");
        assertTrue(sistema.registrarUsuario(av));
        assertTrue(sistema.registrarUsuario(dev));

        assertAll("Buscas, listagens por tipo e login",
            () -> assertEquals(1, Sistema.getClientes().size()),
            () -> assertEquals(1, Sistema.getAvaliadores().size()),
            () -> assertEquals(1, Sistema.getDesenvolvedores().size()),
            () -> assertNotNull(Sistema.getUsuario("  LUCAS@UFMG.BR  ")),
            () -> assertNull(Sistema.getUsuario("naoexiste@ufmg.br")),
            () -> assertNull(Sistema.getUsuario(null)),
            () -> assertFalse(sistema.login("naoexiste@ufmg.br", "123")),
            () -> assertTrue(sistema.login("LUCAS@UFMG.BR", "123"))
        );

        cli1.delete();
        assertNull(Sistema.getUsuario("lucas@ufmg.br"));
        assertTrue(Sistema.getClientes().isEmpty());
    }

    @Test
    @DisplayName("Deve filtrar logs por nível, usuário e ação em ordem cronológica inversa (mais recente primeiro)")
    void deveFiltrarLogsPorNivelUsuarioEAcao() {
        Sistema.registrarLog("INFO", "a@ufmg.br", "LOGIN", "Login 1");
        Sistema.registrarLog("AVISO", "a@ufmg.br", "LOGIN", "Falha login");
        Sistema.registrarLog("INFO", "b@ufmg.br", "CRIAR_DEMANDA", "Demanda criada");
        Sistema.registrarLog("AVISO", "b@ufmg.br", "RETORNAR_DEMANDA", "Demanda retornada");

        List<Log> avisos = Sistema.consultarLogsPorNivel("  aviso ");
        List<Log> logsUserA = Sistema.consultarLogsPorUsuario("A@UFMG.BR");
        List<Log> logsLogin = Sistema.consultarLogsPorAcao("login");

        assertAll("Filtros de auditoria",
            () -> assertEquals(2, avisos.size()),
            () -> assertEquals("RETORNAR_DEMANDA", avisos.get(0).getAcao(), "Mais recente deve vir primeiro"),
            () -> assertEquals(2, logsUserA.size()),
            () -> assertEquals("AVISO", logsLogin.get(0).getNivel()),
            () -> assertTrue(Sistema.consultarLogsPorNivel(null).isEmpty()),
            () -> assertTrue(Sistema.consultarLogsPorUsuario(null).isEmpty()),
            () -> assertTrue(Sistema.consultarLogsPorAcao(null).isEmpty())
        );
    }

    @Test
    @DisplayName("Deve retornar mensagens recebidas e enviadas da mais recente para a mais antiga")
    void deveOrdenarMensagensMaisRecentesPrimeiro() {
        Cliente cli = new Cliente(0, "Lucas", "lucas@ufmg.br", "123", TipoUsuario.Cliente);
        Desenvolvedor dev = new Desenvolvedor(1, "Bruno", "bruno@ufmg.br", "123", TipoUsuario.Desenvolvedor);
        sistema.registrarUsuario(cli);
        sistema.registrarUsuario(dev);

        cli.enviarMensagem("Msg 1", "Primeira", dev);
        cli.enviarMensagem("Msg 2", "Segunda", dev);
        cli.enviarMensagem("Msg 3", "Terceira", dev);

        // Enviar para destinatário nulo não deve lançar exceção
        assertDoesNotThrow(() -> cli.enviarMensagem("Fantasma", "Ignorada", null));

        List<Mensagem> recebidasDev = dev.getMensagensRecebidas();
        List<Mensagem> enviadasCli = cli.getMensagensEnviadas();

        assertAll("Ordenação das caixas de entrada e saída",
            () -> assertEquals(3, recebidasDev.size()),
            () -> assertEquals("Msg 3", recebidasDev.get(0).getTitulo()),
            () -> assertEquals("Msg 1", recebidasDev.get(2).getTitulo()),
            () -> assertEquals(3, enviadasCli.size()),
            () -> assertEquals("Msg 3", enviadasCli.get(0).getTitulo())
        );
    }
}