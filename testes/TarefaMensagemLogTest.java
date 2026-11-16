import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tarefa, mensagem e log")
public class TarefaMensagemLogTest {

    private Cliente cliente;
    private Desenvolvedor dev;
    private Demanda demanda;

    @BeforeEach
    void setUp() {
        TestHelper.resetarAmbiente();
        Demanda.resetarContador();
        cliente = new Cliente(0, "Cliente", "cli@test.com", "123", TipoUsuario.Cliente);
        dev = new Desenvolvedor(1, "Dev", "dev@test.com", "123", TipoUsuario.Desenvolvedor);
        demanda = new Demanda(cliente, "Sistema", "Desc", LocalDateTime.now().plusDays(10));
    }

    @Test
    @DisplayName("Deve criar e atualizar Tarefa corretamente")
    void deveGerenciarCicloDeVidaDaTarefa() {
        Tarefa t = new Tarefa("Criar Banco", "Script SQL", demanda);

        assertEquals(1, t.getId());
        assertEquals(StatusTarefa.EmAndamento, t.getStatus());
        assertEquals(demanda.getId(), t.getDemandaVinculada());

        t.atualizarTarefa("Script PostgreSQL atualizado");
        assertEquals("Script PostgreSQL atualizado", t.getDescricao());

        t.setStatus(StatusTarefa.Concluida);
        assertEquals(StatusTarefa.Concluida, t.getStatus());
        assertTrue(t.toString().contains("Concluida"));
    }

    @Test
    @DisplayName("Deve criar Mensagem não lida por padrão e marcar como lida")
    void deveGerenciarEstadoDeLeituraDaMensagem() {
        Mensagem m = new Mensagem("Dúvida", "Qual o prazo?", cliente, dev);

        assertEquals(0, m.getId());
        assertFalse(m.isLida());
        assertEquals("cli@test.com", m.getEmailRemetente());
        assertEquals("dev@test.com", m.getEmailDestinatario());
        assertNotNull(m.getData());

        m.setLida();
        assertTrue(m.isLida());
    }

    @Test
    @DisplayName("Deve formatar Log corretamente e atribuir SISTEMA quando usuário for null")
    void deveFormatarLogCorretamente() {
        Log logSistema = new Log("INFO", null, "BOOT", "Sistema iniciado");
        assertEquals("SISTEMA", logSistema.getUsuario());
        assertTrue(logSistema.toString().contains("[INFO ] [SISTEMA] [BOOT] Sistema iniciado"));

        Log logUser = new Log("AVISO", "cli@test.com", "LOGIN", "Falha");
        assertEquals("cli@test.com", logUser.getUsuario());
    }
}
