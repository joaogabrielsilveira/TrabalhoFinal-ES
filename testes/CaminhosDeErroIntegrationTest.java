import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Caminhos de erro e exceções de negócio")
public class CaminhosDeErroIntegrationTest {

    private Sistema sistema;
    private Cliente cliente1;
    private Cliente cliente2;

    @BeforeEach
    void setUp() {
        sistema = TestHelper.resetarAmbiente();
        Demanda.resetarContador();
        cliente1 = new Cliente(0, "Cliente 1", "c1@ufmg.br", "123", TipoUsuario.Cliente);
        cliente2 = new Cliente(1, "Cliente 2", "c2@ufmg.br", "123", TipoUsuario.Cliente);
        sistema.registrarUsuario(cliente1);
        sistema.registrarUsuario(cliente2);
    }

    @Test
    @DisplayName("Deve rejeitar criação ou edição de demanda com prazo menor ou igual a zero")
    void deveRejeitarPrazoMenorOuIgualAZero() {
        assertThrows(IllegalArgumentException.class, () -> cliente1.solicitarDemanda("T1", "Desc", 0));
        assertThrows(IllegalArgumentException.class, () -> cliente1.solicitarDemanda("T2", "Desc", -5));

        Demanda valida = cliente1.solicitarDemanda("T3", "Desc", 10);
        assertThrows(IllegalArgumentException.class, () -> cliente1.atualizarDemanda(valida, "Nova", 0));
        assertThrows(IllegalArgumentException.class, () -> cliente1.atualizarDemanda(valida, "Nova", -1));
    }

    @Test
    @DisplayName("Deve manter demanda Aberta sem avaliador e retornar null ao aprovar sem desenvolvedor")
    void deveLidarComAusenciaDeAvaliadorEDesenvolvedor() {
        // Nenhum avaliador cadastrado ainda
        Demanda d = cliente1.solicitarDemanda("Sem Avaliador", "Desc", 10);
        assertEquals(StatusDemanda.Aberta, d.getSituacao());
        assertNull(d.getEmailAvaliador());

        // Cadastra avaliador e coloca demanda na fila dele, mas ainda sem desenvolvedores
        Avaliador av = new Avaliador(2, "Avaliador", "av@ufmg.br", "123", TipoUsuario.Avaliador);
        sistema.registrarUsuario(av);
        av.receberDemanda(d, cliente1);

        Desenvolvedor resultado = av.encaminharDemanda(d);
        assertNull(resultado, "Deve retornar null quando não há desenvolvedor cadastrado");
        assertEquals(StatusDemanda.Aberta, d.getSituacao());
        assertTrue(av.getDemandasParaAprovacao().contains(d));
    }

    @Test
    @DisplayName("Deve impedir cliente de editar demanda de outro cliente ou demanda nula")
    void deveImpedirEdicaoDeDemandaAlheia() {
        Demanda dCliente1 = cliente1.solicitarDemanda("Demanda C1", "Desc", 10);

        assertThrows(IllegalArgumentException.class, () ->
            cliente2.atualizarDemanda(dCliente1, "Hackeado", 15)
        );
        assertThrows(IllegalArgumentException.class, () ->
            cliente1.atualizarDemanda(null, "Nula", 15)
        );
    }

    @Test
    @DisplayName("Deve impedir desenvolvedor de manipular demanda de outro dev ou entregar demanda duas vezes")
    void deveImpedirDevEmDemandaAlheiaEEntregaDuplicada() {
        Avaliador av = new Avaliador(2, "Avaliador", "av@ufmg.br", "123", TipoUsuario.Avaliador);
        Desenvolvedor dev1 = new Desenvolvedor(3, "Dev 1", "dev1@ufmg.br", "123", TipoUsuario.Desenvolvedor);
        Desenvolvedor dev2 = new Desenvolvedor(4, "Dev 2", "dev2@ufmg.br", "123", TipoUsuario.Desenvolvedor);

        sistema.registrarUsuario(av);
        sistema.registrarUsuario(dev1);
        sistema.registrarUsuario(dev2);

        Demanda d = cliente1.solicitarDemanda("Feature X", "Desc", 10);
        Desenvolvedor alocado = av.encaminharDemanda(d);
        Desenvolvedor naoAlocado = alocado.equals(dev1) ? dev2 : dev1;

        // Outro desenvolvedor não pode criar tarefa nem entregar a demanda
        assertThrows(IllegalStateException.class, () ->
            naoAlocado.criarTarefa("Invasão", "Desc", d)
        );
        assertThrows(IllegalStateException.class, () ->
            naoAlocado.entregarDemanda(d)
        );

        // Desenvolvedor correto entrega uma vez com sucesso
        alocado.entregarDemanda(d);
        assertEquals(StatusDemanda.Finalizada, d.getSituacao());

        // Tentar entregar a mesma demanda pela segunda vez deve falhar
        assertThrows(IllegalStateException.class, () ->
            alocado.entregarDemanda(d)
        );
    }
}