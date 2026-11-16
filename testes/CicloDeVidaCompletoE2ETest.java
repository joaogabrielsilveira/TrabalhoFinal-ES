import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Jornada completa de negócio e auditoria")
public class CicloDeVidaCompletoE2ETest {

    @BeforeEach
    void setUp() {
        TestHelper.resetarAmbiente();
        Demanda.resetarContador();
    }

    @Test
    @DisplayName("Fluxo completo desde cadastro, solicitação, retorno, aprovação, execução de tarefas até entrega")
    void jornadaCompletaDeDemandaComRetornoETarefas() {
        Sistema sistema = new Sistema();

        // 1. Cadastro dos atores
        Cliente cliente = new Cliente(0, "Lucas", "lucas@cliente.com", "pass123", TipoUsuario.Cliente);
        Avaliador avaliador = new Avaliador(1, "Michele", "michele@avaliador.com", "pass123", TipoUsuario.Avaliador);
        Desenvolvedor dev = new Desenvolvedor(2, "Guilherme", "guilherme@dev.com", "pass123", TipoUsuario.Desenvolvedor);

        assertTrue(sistema.registrarUsuario(cliente));
        assertTrue(sistema.registrarUsuario(avaliador));
        assertTrue(sistema.registrarUsuario(dev));
        assertFalse(sistema.registrarUsuario(cliente), "Não deve permitir e-mail duplicado");

        // 2. Autenticação
        assertTrue(sistema.login("lucas@cliente.com", "pass123"));
        assertFalse(sistema.login("lucas@cliente.com", "senha_errada"));

        // 3. Cliente abre demanda -> cai automaticamente na fila do avaliador
        Demanda demanda = cliente.solicitarDemanda("Módulo de Relatórios", "Gerar PDF", 10);
        assertEquals(StatusDemanda.Aberta, demanda.getSituacao());
        assertEquals(1, avaliador.getDemandasParaAprovacao().size());

        // 4. Avaliador classifica prioridade e retorna para ajustes
        avaliador.atualizarPrioridade(demanda);
        assertEquals(PrioridadeDemanda.Alta, demanda.getPrioridade());

        avaliador.retornarDemanda(demanda, "Detalhar quais campos devem constar no PDF.");
        assertEquals(StatusDemanda.Retornada, demanda.getSituacao());
        assertEquals(0, avaliador.getDemandasParaAprovacao().size());

        // 5. Cliente edita demanda retornada -> reencaminha automaticamente ao avaliador
        cliente.atualizarDemanda(demanda, "Gerar PDF com ID, título, status, prazo e responsável.", 12);
        assertEquals(StatusDemanda.Aberta, demanda.getSituacao());
        assertEquals(1, avaliador.getDemandasParaAprovacao().size());

        // 6. Avaliador aprova e encaminha ao desenvolvedor
        Desenvolvedor devAlocado = avaliador.encaminharDemanda(demanda);
        assertNotNull(devAlocado);
        assertEquals("guilherme@dev.com", devAlocado.getEmail());
        assertEquals(StatusDemanda.EmAndamento, demanda.getSituacao());
        assertEquals(1, avaliador.getDemandasAprovadas().size());
        assertEquals(1, dev.getDemandasAtribuidas().size());

        // 7. Cliente envia dúvida direta ao desenvolvedor alocado e recebe resposta
        cliente.enviarMensagem("Dúvida sobre layout", "O logo deve ir no cabeçalho?", dev);
        List<Mensagem> inboxDev = dev.getMensagensRecebidas();
        assertTrue(inboxDev.stream().anyMatch(m -> m.getTitulo().equals("Dúvida sobre layout")));
        dev.enviarMensagem("Re: Dúvida sobre layout", "Sim, já incluído no template.", cliente);

        // 8. Desenvolvedor cria, atualiza e conclui tarefas técnicas
        Tarefa t1 = dev.criarTarefa("Criar template HTML/PDF", "Layout base", demanda);
        Tarefa t2 = dev.criarTarefa("Integrar consulta de dados", "Buscar demandas no Sistema", demanda);
        assertEquals(2, demanda.getTarefas().size());

        dev.atualizarTarefa(t1, "Layout base com logotipo no cabeçalho");
        dev.finalizarTarefa(t1);
        dev.finalizarTarefa(t2);
        assertEquals(StatusTarefa.Concluida, t1.getStatus());
        assertEquals(StatusTarefa.Concluida, t2.getStatus());

        // 9. Desenvolvedor entrega a demanda -> finaliza demanda, limpa tarefas ativas e notifica cliente
        dev.entregarDemanda(demanda);
        assertEquals(StatusDemanda.Finalizada, demanda.getSituacao());
        assertEquals(0, dev.getCargaAtual());
        assertTrue(demanda.getTarefas().isEmpty(), "Tarefas da demanda finalizada devem ser removidas");
        assertTrue(cliente.getMensagensRecebidas().stream().anyMatch(m -> m.getTitulo().equals("Demanda finalizada")));

        // 10. Desenvolvedor não pode mais criar tarefas em demanda finalizada
        assertThrows(IllegalStateException.class, () ->
            dev.criarTarefa("Tarefa tardia", "Não permitida", demanda)
        );

        // 11. Auditoria de logs
        assertFalse(Sistema.consultarLogsPorAcao("REGISTRO").isEmpty());
        assertFalse(Sistema.consultarLogsPorAcao("CRIAR_DEMANDA").isEmpty());
        assertFalse(Sistema.consultarLogsPorAcao("CRIAR_TAREFA").isEmpty());
        assertFalse(Sistema.consultarLogsPorAcao("ENVIAR_MENSAGEM").isEmpty());
    }
}
