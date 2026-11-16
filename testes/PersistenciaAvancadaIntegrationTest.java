import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Persistência de mensagens lidas, logs, listas nulas e contadores")
public class PersistenciaAvancadaIntegrationTest {

    @BeforeEach
    void setUp() {
        TestHelper.resetarAmbiente();
        Demanda.resetarContador();
    }

    @Test
    @DisplayName("Deve persistir estado 'lida' das mensagens, logs e continuidade de todos os IDs após reinicialização")
    void devePersistirMensagensLidasLogsEContadores() {
        Sistema s1 = new Sistema();

        Cliente cli = new Cliente(0, "Ana", "ana@ufmg.br", "123", TipoUsuario.Cliente);
        Avaliador av = new Avaliador(1, "Bruno", "bruno@ufmg.br", "123", TipoUsuario.Avaliador);
        Desenvolvedor dev = new Desenvolvedor(2, "Carla", "carla@ufmg.br", "123", TipoUsuario.Desenvolvedor);

        s1.registrarUsuario(cli);
        s1.registrarUsuario(av);
        s1.registrarUsuario(dev);

        Demanda d = cli.solicitarDemanda("Sistema", "Desc", 20);
        av.encaminharDemanda(d);
        Tarefa t = dev.criarTarefa("Tarefa 1", "Desc", d);

        // Marca a primeira mensagem recebida pelo cliente como lida e salva
        Mensagem primeiraMsg = cli.getMensagensRecebidas().get(0);
        primeiraMsg.setLida();
        Sistema.salvarMensagens();

        int qtdLogsAntes = Sistema.getLogs().size();
        int ultimoUsuarioId = dev.getMatricula();
        int ultimaDemandaId = d.getId();
        int ultimaTarefaId = t.getId();
        int ultimaMensagemId = cli.getMensagensRecebidas().get(0).getId();

        // Reinicia o sistema (carrega tudo do disco)
        Sistema s2 = new Sistema();

        Cliente cli2 = (Cliente) Sistema.getUsuario("ana@ufmg.br");
        List<Mensagem> msgsRestauradas = cli2.getMensagensRecebidas();

        assertAll("Estado restaurado do disco",
            () -> assertEquals(qtdLogsAntes, Sistema.getLogs().size()),
            () -> assertTrue(msgsRestauradas.stream().anyMatch(Mensagem::isLida), "Estado lida deve persistir"),
            () -> assertEquals(1, Sistema.getTarefas().size())
        );

        // Cria novos registros após o restart para garantir que nenhum contador colide
        Cliente novoCli = new Cliente(0, "Diego", "diego@ufmg.br", "123", TipoUsuario.Cliente);
        s2.registrarUsuario(novoCli);
        Demanda novaDemanda = novoCli.solicitarDemanda("Nova", "Desc", 10);
        Avaliador av2 = (Avaliador) Sistema.getUsuario("bruno@ufmg.br");
        Desenvolvedor dev2 = av2.encaminharDemanda(novaDemanda);
        Tarefa novaTarefa = dev2.criarTarefa("Tarefa 2", "Desc", novaDemanda);
        Mensagem novaMsg = novoCli.getMensagensRecebidas().get(0);

        assertAll("Continuidade sem colisão dos 4 contadores estáticos",
            () -> assertTrue(novoCli.getMatricula() > ultimoUsuarioId),
            () -> assertEquals(ultimaDemandaId + 1, novaDemanda.getId()),
            () -> assertEquals(ultimaTarefaId + 1, novaTarefa.getId()),
            () -> assertTrue(novaMsg.getId() > ultimaMensagemId)
        );
    }

    @Test
    @DisplayName("Deve inicializar listas internas quando o JSON carregado não possuir os campos de lista (aposCarregar)")
    void deveInicializarListasNulasAoCarregarJsonIncompleto() throws IOException {
        Path pastaTemp = Files.createTempDirectory("json-nulo-");
        Banco.setDiretorio(pastaTemp.toString());

        // JSON gravado manualmente sem os campos de listas (simula migração ou JSON legado)
        String jsonSemListas = """
            [
              { "matricula": 0, "nome": "Cli", "email": "c@ufmg.br", "senha": "x", "type": "Cliente" },
              { "matricula": 1, "nome": "Ava", "email": "a@ufmg.br", "senha": "x", "type": "Avaliador" },
              { "matricula": 2, "nome": "Dev", "email": "d@ufmg.br", "senha": "x", "type": "Desenvolvedor" }
            ]
            """;
        Files.writeString(pastaTemp.resolve("usuarios.json"), jsonSemListas);

        Sistema s = new Sistema();
        Cliente c = (Cliente) Sistema.getUsuario("c@ufmg.br");
        Avaliador a = (Avaliador) Sistema.getUsuario("a@ufmg.br");
        Desenvolvedor d = (Desenvolvedor) Sistema.getUsuario("d@ufmg.br");

        assertAll("Listas não nulas garantidas por aposCarregar()",
            () -> assertNotNull(c.getDemandasSolicitadas()),
            () -> assertNotNull(a.getDemandasParaAprovacao()),
            () -> assertNotNull(a.getDemandasAprovadas()),
            () -> assertNotNull(d.getDemandasAtribuidas())
        );
    }
}