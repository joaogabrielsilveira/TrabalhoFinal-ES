import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Aprovação, retorno e balanceamento de carga")
public class FluxoAprovacaoBalanceamentoIntegrationTest {

    private Sistema sistema;
    private Cliente cliente;
    private Avaliador avaliador1;
    private Avaliador avaliador2;
    private Desenvolvedor dev1;
    private Desenvolvedor dev2;

    @BeforeEach
    void setUp() {
        sistema = TestHelper.resetarAmbiente();
        Demanda.resetarContador();
        cliente = new Cliente(0, "Lucas", "lucas@ufmg.br", "senha123", TipoUsuario.Cliente);
        avaliador1 = new Avaliador(1, "Avaliador 1", "av1@ufmg.br", "senha123", TipoUsuario.Avaliador);
        avaliador2 = new Avaliador(2, "Avaliador 2", "av2@ufmg.br", "senha123", TipoUsuario.Avaliador);
        dev1 = new Desenvolvedor(3, "Dev 1", "dev1@ufmg.br", "senha123", TipoUsuario.Desenvolvedor);
        dev2 = new Desenvolvedor(4, "Dev 2", "dev2@ufmg.br", "senha123", TipoUsuario.Desenvolvedor);

        sistema.registrarUsuario(cliente);
        sistema.registrarUsuario(avaliador1);
        sistema.registrarUsuario(avaliador2);
        sistema.registrarUsuario(dev1);
        sistema.registrarUsuario(dev2);
    }

    @Test
    @DisplayName("Deve distribuir demandas entre avaliadores menos carregados na criação")
    void deveBalancearDistribuicaoEntreAvaliadores() {
        Demanda d1 = cliente.solicitarDemanda("Demanda 1", "Desc 1", 10);
        Demanda d2 = cliente.solicitarDemanda("Demanda 2", "Desc 2", 15);

        assertEquals(1, avaliador1.getDemandasParaAprovacao().size());
        assertEquals(1, avaliador2.getDemandasParaAprovacao().size());
        assertEquals("av1@ufmg.br", d1.getEmailAvaliador());
        assertEquals("av2@ufmg.br", d2.getEmailAvaliador());
    }

    @Test
    @DisplayName("Deve retornar demanda para o cliente e reencaminhar ao MESMO avaliador após edição")
    void deveRetornarEReencaminharParaMesmoAvaliador() {
        // Cria 3 demandas para distribuir entre os dois avaliadores:
        // d1 -> av1 (fila: 1), d2 -> av2 (fila: 1), d3 -> av1 (fila: 2)
        Demanda d1 = cliente.solicitarDemanda("Demanda 1", "Desc 1", 10);
        Demanda d2 = cliente.solicitarDemanda("Demanda 2", "Desc 2", 10);
        Demanda d3 = cliente.solicitarDemanda("Demanda 3", "Desc 3", 10);

        // av1 retorna d1 -> av1 continua com d3 na fila (carga 1)
        avaliador1.retornarDemanda(d1, "Faltou especificar requisitos não funcionais");
        assertEquals(StatusDemanda.Retornada, d1.getSituacao());
        assertEquals(1, avaliador1.getDemandasParaAprovacao().size());

        // av2 aprova d2 -> fila de av2 cai para 0 (estritamente mais livre que av1)
        avaliador2.encaminharDemanda(d2);
        assertEquals(0, avaliador2.getDemandasParaAprovacao().size());

        // Verifica notificação enviada ao cliente
        List<Mensagem> msgsCliente = cliente.getMensagensRecebidas();
        assertTrue(msgsCliente.stream().anyMatch(m -> m.getTitulo().equals("Demanda retornada")));

        // Cliente edita d1; mesmo av2 estando estritamente mais livre (0 < 1),
        // d1 deve voltar para av1 por ser o avaliador anterior
        cliente.atualizarDemanda(d1, "Desc 1 completa com RNFs", 12);

        assertEquals(StatusDemanda.Aberta, d1.getSituacao());
        assertEquals("av1@ufmg.br", d1.getEmailAvaliador());
        assertTrue(avaliador1.getDemandasParaAprovacao().contains(d1));
        assertFalse(avaliador2.getDemandasParaAprovacao().contains(d1));
    }

    @Test
    @DisplayName("Deve balancear atribuição para o desenvolvedor com menor carga em andamento")
    void deveBalancearCargaEntreDesenvolvedores() {
        Demanda d1 = cliente.solicitarDemanda("D1", "Desc", 10);
        Demanda d2 = cliente.solicitarDemanda("D2", "Desc", 10);
        Demanda d3 = cliente.solicitarDemanda("D3", "Desc", 10);

        Avaliador avD1 = (Avaliador) Sistema.getUsuario(d1.getEmailAvaliador());
        Avaliador avD2 = (Avaliador) Sistema.getUsuario(d2.getEmailAvaliador());
        Avaliador avD3 = (Avaliador) Sistema.getUsuario(d3.getEmailAvaliador());

        Desenvolvedor escolhido1 = avD1.encaminharDemanda(d1);
        Desenvolvedor escolhido2 = avD2.encaminharDemanda(d2);

        assertNotEquals(escolhido1.getEmail(), escolhido2.getEmail());
        assertEquals(1, dev1.getCargaAtual());
        assertEquals(1, dev2.getCargaAtual());

        // Finaliza d1 no dev1 -> carga atual de dev1 cai para 0
        dev1.entregarDemanda(d1);
        assertEquals(0, dev1.getCargaAtual());
        assertEquals(1, dev2.getCargaAtual());

        // d3 deve ir para dev1 (menor carga atual)
        Desenvolvedor escolhido3 = avD3.encaminharDemanda(d3);
        assertEquals(dev1.getEmail(), escolhido3.getEmail());
    }

    @Test
    @DisplayName("Deve impedir edição e reenvio de demanda cancelada pelo avaliador")
    void deveBloquearDemandaCancelada() {
        Demanda d = cliente.solicitarDemanda("Demanda Inválida", "Escopo fora", 5);
        Avaliador av = (Avaliador) Sistema.getUsuario(d.getEmailAvaliador());

        av.cancelarDemanda(d, "Fora do escopo do projeto");

        assertEquals(StatusDemanda.Cancelada, d.getSituacao());
        assertTrue(av.getDemandasParaAprovacao().isEmpty());

        assertThrows(IllegalStateException.class, () ->
            cliente.atualizarDemanda(d, "Tentativa de edição", 10)
        );
        assertThrows(IllegalStateException.class, () ->
            av.receberDemanda(d, cliente)
        );
    }
}
