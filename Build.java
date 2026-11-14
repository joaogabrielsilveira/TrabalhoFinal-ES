import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Build multiplataforma (Windows, macOS e Linux). Só precisa de um JDK 17+ no PATH.
 * Não depende de shell, make, Maven ou Gradle: roda como programa Java de arquivo único.
 *
 * Uso (na pasta do projeto, onde ficam os .java e a pasta lib/):
 *
 *     java Build.java            compila e executa (igual a "run")
 *     java Build.java run        compila e executa o sistema
 *     java Build.java build      só compila (gera os .class em out/)
 *     java Build.java clean      apaga a pasta out/
 *
 * Estrutura esperada:
 *     lib/*.jar        dependências (gson, spring-jcl, spring-security-crypto)
 *     *.java ou src/   código-fonte (se existir a pasta src/, ela é usada; senão, a raiz)
 *     database/        criada em tempo de execução, relativa à pasta onde o comando é rodado
 */
public class Build {

    static final Path RAIZ = Path.of("").toAbsolutePath();
    static final Path LIB = RAIZ.resolve("lib");
    static final Path OUT = RAIZ.resolve("out");
    static final String CLASSE_PRINCIPAL = "Main";
    static final int JAVA_MINIMO = 17;

    public static void main(String[] args) throws Exception {
        String comando = args.length == 0 ? "run" : args[0].toLowerCase();

        switch (comando) {
            case "clean" -> limpar();
            case "build" -> System.exit(compilar() ? 0 : 1);
            case "run" -> {
                if (!compilar()) System.exit(1);
                System.exit(executar());
            }
            default -> {
                System.out.println("Comandos: run (padrão) | build | clean");
                System.exit(comando.equals("help") ? 0 : 1);
            }
        }
    }

    // ------------------------------------------------------------------ comandos

    static boolean compilar() throws IOException {
        if (Runtime.version().feature() < JAVA_MINIMO) {
            System.err.println("Java " + JAVA_MINIMO + "+ é necessário (encontrado: " + Runtime.version() + ").");
            return false;
        }
        JavaCompiler compilador = ToolProvider.getSystemJavaCompiler();
        if (compilador == null) {
            System.err.println("Compilador não encontrado: instale um JDK (não apenas o JRE) e use-o no PATH.");
            return false;
        }

        List<Path> jars = listarJars();
        if (jars.isEmpty()) {
            System.err.println("Aviso: nenhum .jar encontrado em " + LIB + " (gson, spring-jcl e spring-security-crypto são necessários).");
        }

        List<Path> fontes = listarFontes();
        if (fontes.isEmpty()) {
            System.err.println("Nenhum arquivo .java encontrado.");
            return false;
        }

        limpar(); // build limpo: evita .class antigos de classes removidas
        Files.createDirectories(OUT);

        List<String> opcoes = List.of(
            "-encoding", "UTF-8",               // acentos nos fontes (ex.: "Média") em qualquer SO
            "-d", OUT.toString(),
            "-cp", classpath(jars)
        );

        try (StandardJavaFileManager fm = compilador.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> unidades = fm.getJavaFileObjectsFromPaths(fontes);
            boolean ok = compilador.getTask(null, fm, null, opcoes, null, unidades).call();
            System.out.println(ok ? "Compilação concluída (" + fontes.size() + " arquivos) -> " + OUT
                                  : "Falha na compilação.");
            return ok;
        }
    }

    static int executar() throws IOException, InterruptedException {
        String exe = System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
        String java = Path.of(System.getProperty("java.home"), "bin", exe).toString();

        List<String> cmd = new ArrayList<>(List.of(java, "-cp", classpath(listarJars()), CLASSE_PRINCIPAL));
        // inheritIO: o programa usa o mesmo terminal (Scanner lê o teclado normalmente)
        return new ProcessBuilder(cmd).directory(RAIZ.toFile()).inheritIO().start().waitFor();
    }

    static void limpar() throws IOException {
        if (!Files.exists(OUT)) return;
        try (Stream<Path> arvore = Files.walk(OUT)) {
            for (Path p : arvore.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(p);
            }
        }
    }

    // ------------------------------------------------------------------ utilitários

    /** Separador de classpath correto por SO: ';' no Windows, ':' no Linux/macOS. */
    static String classpath(List<Path> jars) {
        List<String> partes = new ArrayList<>();
        partes.add(OUT.toString());
        for (Path jar : jars) partes.add(jar.toString());
        return String.join(File.pathSeparator, partes);
    }

    static List<Path> listarJars() throws IOException {
        if (!Files.isDirectory(LIB)) return List.of();
        try (Stream<Path> s = Files.list(LIB)) {
            return s.filter(p -> p.toString().endsWith(".jar")).sorted().collect(Collectors.toList());
        }
    }

    static List<Path> listarFontes() throws IOException {
        Path src = RAIZ.resolve("src");
        boolean usaSrc = Files.isDirectory(src);
        Path base = usaSrc ? src : RAIZ;
        try (Stream<Path> s = Files.walk(base, usaSrc ? Integer.MAX_VALUE : 1)) {
            return s.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.getFileName().toString().equals("Build.java"))
                    .sorted().collect(Collectors.toList());
        }
    }
}
