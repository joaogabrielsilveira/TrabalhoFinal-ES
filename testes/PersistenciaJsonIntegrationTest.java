import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Persistência JSON (Banco <-> Sistema)")
public class PersistenciaJsonIntegrationTest {

    @BeforeEach
    void setUp() {
        TestHelper.resetarAmbiente();
        Demanda.resetarContador();
    }

    @Test
    @DisplayName("Deve persistir usuários polimórficos, demandas, tarefas, mensagens e logs no disco e restaurar estado")
    void devePersistirERestaurarEstadoCompletoDoSistema() {
        Sistema s1 = new Sistema();

        Cliente cli = new Cliente(0, "Ana", "ana@ufmg.br", "ana123", TipoUsuario.Cliente);
        Avaliador av = new Avaliador(1, "Bruno", "bruno@ufmg.br", "bruno123", TipoUsuario.Avaliador);
        Desenvolvedor dev = new Desenvolvedor(2, "Carla", "carla@ufmg.br", "carla123", TipoUsuario.Desenvolvedor);

        s1.registrarUsuario(cli);
        s1.registrarUsuario(av);
        s1.registrarUsuario(dev);

        Demanda d = cli.solicitarDemanda("API REST", "Implementar endpoints", 25);
        av.atualizarPrioridade(d);
        av.encaminharDemanda(d);
        dev.criarTarefa("Criar Controller", "Endpoints GET/POST", d);

        // Simula reinicialização da aplicação criando uma nova instância de Sistema (lê JSONs do disco)
        Sistema s2 = new Sistema();
        Usuario cliRestaurado = Sistema.getUsuario("ana@ufmg.br");
        Demanda demandaRestaurada = Sistema.getDemanda(d.getId());

        assertAll("Restauração de usuários e credenciais",
            () -> assertTrue(s2.login("ana@ufmg.br", "ana123")),
            () -> assertTrue(s2.login("bruno@ufmg.br", "bruno123")),
            () -> assertTrue(s2.login("carla@ufmg.br", "carla123")),
            () -> assertInstanceOf(Cliente.class, cliRestaurado),
            () -> assertEquals(1, ((Cliente) cliRestaurado).getDemandasSolicitadas().size())
        );

        assertAll("Restauração de demanda, prioridade e tarefas",
            () -> assertNotNull(demandaRestaurada),
            () -> assertEquals("API REST", demandaRestaurada.getTitulo()),
            () -> assertEquals(StatusDemanda.EmAndamento, demandaRestaurada.getSituacao()),
            () -> assertEquals(PrioridadeDemanda.Média, demandaRestaurada.getPrioridade()),
            () -> assertEquals(1, demandaRestaurada.getTarefas().size()),
            () -> assertEquals("Criar Controller", demandaRestaurada.getTarefas().get(0).getTitulo())
        );

        // Garante que os contadores estáticos não colidem após recarregar do banco
        Demanda dNova = ((Cliente) cliRestaurado).solicitarDemanda("Nova Demanda", "Após restart", 7);
        assertAll("Continuidade de contadores e logs",
            () -> assertEquals(d.getId() + 1, dNova.getId()),
            () -> assertFalse(Sistema.getLogs().isEmpty())
        );
    }
}