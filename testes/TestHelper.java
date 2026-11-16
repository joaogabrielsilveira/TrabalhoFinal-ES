import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Utilitário compartilhado pelos testes para garantir isolamento total:
 * redireciona o Banco para uma pasta temporária a cada teste (protegendo a pasta database/ real)
 * e reinicializa os contadores estáticos.
 */
public class TestHelper {

    private static Path pastaAtual;

    static {
        // Garante que a pasta temporária do último teste executado também seja removida ao encerrar a JVM
        Runtime.getRuntime().addShutdownHook(new Thread(() -> apagar(pastaAtual)));
    }

    public static Sistema resetarAmbiente() {
        try {
            apagar(pastaAtual); // limpa a pasta do teste anterior
            pastaAtual = Files.createTempDirectory("tp-testes-");
            Banco.setDiretorio(pastaAtual.resolve("database").toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        Usuario.restaurarContador(0);
        Demanda.ID = 0;
        Tarefa.ID = 0;
        Mensagem.ID = 0;
        return new Sistema();
    }

    private static void apagar(Path dir) {
        if (dir == null || !Files.exists(dir)) return;
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }
}