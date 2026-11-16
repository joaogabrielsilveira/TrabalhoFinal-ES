import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Demanda")
public class DemandaTest {

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        TestHelper.resetarAmbiente();
        Demanda.resetarContador();
        cliente = new Cliente(0, "João", "joao@teste.com", "senha123", TipoUsuario.Cliente);
    }

    @Test
    @DisplayName("Deve criar demanda com status Aberta e ID incremental")
    void deveCriarDemandaComValoresIniciaisCorretos() {
        Demanda d1 = new Demanda(cliente, "App Mobile", "Criar app", LocalDateTime.now().plusDays(10));
        Demanda d2 = new Demanda(cliente, "Site Web", "Criar site", LocalDateTime.now().plusDays(20));

        assertAll("Atributos iniciais da demanda",
            () -> assertEquals(0, d1.getId()),
            () -> assertEquals(1, d2.getId()),
            () -> assertEquals(StatusDemanda.Aberta, d1.getSituacao()),
            () -> assertEquals("joao@teste.com", d1.getEmailSolicitante()),
            () -> assertEquals(cliente.getMatricula(), d1.getMatriculaSolicitante()),
            () -> assertTrue(d1.podeSerEditada())
        );
    }

    @Test
    @DisplayName("Deve permitir edição apenas quando Aberta ou Retornada")
    void deveValidarPermissaoDeEdicaoPorStatus() {
        Demanda d = new Demanda(cliente, "Título", "Desc original", LocalDateTime.now().plusDays(10));

        assertAll("Edição permitida em Aberta e Retornada",
            () -> assertTrue(d.podeSerEditada()),
            () -> {
                d.editarDados("Desc editada 1", LocalDateTime.now().plusDays(15));
                assertEquals("Desc editada 1", d.getDescricao());
            },
            () -> {
                d.setSituacao(StatusDemanda.Retornada);
                assertTrue(d.podeSerEditada());
                d.editarDados("Desc editada 2", LocalDateTime.now().plusDays(20));
                assertEquals("Desc editada 2", d.getDescricao());
            }
        );

        assertAll("Bloqueio de edição em EmAndamento e Finalizada",
            () -> {
                d.setSituacao(StatusDemanda.EmAndamento);
                assertFalse(d.podeSerEditada());
                assertThrows(IllegalStateException.class, () ->
                    d.editarDados("Tentativa inválida", LocalDateTime.now().plusDays(25))
                );
            },
            () -> {
                d.setSituacao(StatusDemanda.Finalizada);
                assertFalse(d.podeSerEditada());
                assertThrows(IllegalStateException.class, () ->
                    d.editarDados("Tentativa inválida", LocalDateTime.now().plusDays(25))
                );
            }
        );
    }

    @Test
    @DisplayName("Deve permitir cancelar demanda Aberta ou Retornada e bloquear cancelamento após aprovação")
    void deveValidarRegrasDeCancelamento() {
        Demanda d1 = new Demanda(cliente, "D1", "Desc", LocalDateTime.now().plusDays(5));
        Demanda d2 = new Demanda(cliente, "D2", "Desc", LocalDateTime.now().plusDays(5));
        d2.setSituacao(StatusDemanda.Retornada);
        Demanda d3 = new Demanda(cliente, "D3", "Desc", LocalDateTime.now().plusDays(5));
        d3.setSituacao(StatusDemanda.EmAndamento);

        assertAll("Regras de cancelamento por status",
            () -> {
                d1.cancelarDemanda();
                assertEquals(StatusDemanda.Cancelada, d1.getSituacao());
                assertFalse(d1.podeSerEditada());
            },
            () -> {
                d2.cancelarDemanda();
                assertEquals(StatusDemanda.Cancelada, d2.getSituacao());
            },
            () -> assertThrows(IllegalStateException.class, d3::cancelarDemanda)
        );
    }

    @Test
    @DisplayName("Deve finalizar demanda corretamente")
    void deveFinalizarDemanda() {
        Demanda d = new Demanda(cliente, "D1", "Desc", LocalDateTime.now().plusDays(5));
        d.setSituacao(StatusDemanda.EmAndamento);
        d.finalizarDemanda();

        assertAll("Estado após finalização",
            () -> assertEquals(StatusDemanda.Finalizada, d.getSituacao()),
            () -> assertFalse(d.podeSerEditada())
        );
    }
}