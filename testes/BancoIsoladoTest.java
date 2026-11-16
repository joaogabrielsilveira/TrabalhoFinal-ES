import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Banco isolado")
public class BancoIsoladoTest {

    private Path dirTemporario;

    @BeforeEach
    void setUp() throws IOException {
        TestHelper.resetarAmbiente();
        Demanda.resetarContador();
        dirTemporario = Files.createTempDirectory("banco-isolado-");
        Banco.setDiretorio(dirTemporario.toString());
    }

    @Test
    @DisplayName("Deve retornar listas vazias quando os arquivos JSON ainda não existem")
    void deveLidarComArquivosAusentes() {
        assertAll("Leitura de banco inexistente",
            () -> assertTrue(Banco.carregarUsuarios().isEmpty()),
            () -> assertTrue(Banco.carregarDemandas().isEmpty()),
            () -> assertTrue(Banco.carregarTarefas().isEmpty()),
            () -> assertTrue(Banco.carregarMensagens().isEmpty()),
            () -> assertTrue(Banco.carregarLogs().isEmpty())
        );
    }

    @Test
    @DisplayName("Deve realizar ida e volta (salvar/carregar) por entidade e não deixar arquivos .tmp")
    void deveSalvarECarregarPorEntidadeSemArquivosTemporarios() throws IOException {
        Cliente cli = new Cliente(0, "Lucas", "lucas@ufmg.br", "123", TipoUsuario.Cliente);
        Avaliador av = new Avaliador(1, "Michele", "michele@ufmg.br", "123", TipoUsuario.Avaliador);
        Desenvolvedor dev = new Desenvolvedor(2, "Bruno", "bruno@ufmg.br", "123", TipoUsuario.Desenvolvedor);

        Demanda d = new Demanda(cli, "Demanda Banco", "Teste I/O", LocalDateTime.now().plusDays(10));
        Tarefa t = new Tarefa("Tarefa Banco", "Desc tarefa", d);
        Mensagem m = new Mensagem("Assunto", "Corpo", cli, dev);
        Log l = new Log("INFO", "lucas@ufmg.br", "TESTE_BANCO", "Detalhe");

        Banco.salvarUsuarios(List.of(cli, av, dev));
        Banco.salvarDemandas(List.of(d));
        Banco.salvarTarefas(List.of(t));
        Banco.salvarMensagens(List.of(m));
        Banco.salvarLogs(List.of(l));

        List<Usuario> usuariosLidos = Banco.carregarUsuarios();
        List<Demanda> demandasLidas = Banco.carregarDemandas();
        List<Tarefa> tarefasLidas = Banco.carregarTarefas();
        List<Mensagem> mensagensLidas = Banco.carregarMensagens();
        List<Log> logsLidos = Banco.carregarLogs();

        assertAll("Ida e volta das entidades",
            () -> assertEquals(3, usuariosLidos.size()),
            () -> assertInstanceOf(Cliente.class, usuariosLidos.get(0)),
            () -> assertInstanceOf(Avaliador.class, usuariosLidos.get(1)),
            () -> assertInstanceOf(Desenvolvedor.class, usuariosLidos.get(2)),
            () -> assertEquals(1, demandasLidas.size()),
            () -> assertEquals("Demanda Banco", demandasLidas.get(0).getTitulo()),
            () -> assertEquals(1, tarefasLidas.size()),
            () -> assertEquals("Tarefa Banco", tarefasLidas.get(0).getTitulo()),
            () -> assertEquals(1, mensagensLidas.size()),
            () -> assertEquals("Assunto", mensagensLidas.get(0).getTitulo()),
            () -> assertEquals(1, logsLidos.size()),
            () -> assertEquals("TESTE_BANCO", logsLidos.get(0).getAcao())
        );

        try (Stream<Path> arquivos = Files.list(dirTemporario)) {
            boolean existeTmp = arquivos.anyMatch(p -> p.toString().endsWith(".tmp"));
            assertFalse(existeTmp, "Nenhum arquivo .tmp deve permanecer após a escrita atômica");
        }
    }
}