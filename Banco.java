import com.google.gson.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Camada de persistência.
 *
 * Regra: o Banco só LÊ no início do programa e só GRAVA "fotografias" das coleções
 * que vivem em memória no Sistema. Ele nunca devolve objetos para substituir
 * os que já estão em uso, então não existem mais cópias divergentes.
 */
public class Banco {

    private static final String DIR = "database";
    private static final String ARQ_USUARIOS = DIR + "/usuarios.json";
    private static final String ARQ_DEMANDAS = DIR + "/demandas.json";
    private static final String ARQ_TAREFAS = DIR + "/tarefas.json";
    private static final String ARQ_MENSAGENS = DIR + "/mensagens.json";
    private static final String ARQ_LOGS = DIR + "/logs.json";


    private static final Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(Usuario.class, new JsonDeserializer<Usuario>() {
            @Override
            public Usuario deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = json.getAsJsonObject();

                if (jsonObject.has("type") && !jsonObject.get("type").isJsonNull()) {
                    String tipoStr = jsonObject.get("type").getAsString();

                    if (tipoStr.equalsIgnoreCase("Cliente")) {
                        return context.deserialize(json, Cliente.class);
                    } else if (tipoStr.equalsIgnoreCase("Avaliador")) {
                        return context.deserialize(json, Avaliador.class);
                    } else if (tipoStr.equalsIgnoreCase("Desenvolvedor")) {
                        return context.deserialize(json, Desenvolvedor.class);
                    }
                }

                throw new JsonParseException("Usuário sem campo 'type' válido: " + json);
            }
        })
        .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
        })
        .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
        })
        .create();

    // ---------- leitura (chamada uma única vez, no construtor do Sistema) ----------

    public static List<Usuario> carregarUsuarios() {
        Usuario[] array = gson.fromJson(ler(ARQ_USUARIOS), Usuario[].class);
        return (array != null) ? new ArrayList<>(Arrays.asList(array)) : new ArrayList<>();
    }

    public static List<Demanda> carregarDemandas() {
        Demanda[] array = gson.fromJson(ler(ARQ_DEMANDAS), Demanda[].class);
        return (array != null) ? new ArrayList<>(Arrays.asList(array)) : new ArrayList<>();
    }

    public static List<Tarefa> carregarTarefas() {
        Tarefa[] array = gson.fromJson(ler(ARQ_TAREFAS), Tarefa[].class);
        return (array != null) ? new ArrayList<>(Arrays.asList(array)) : new ArrayList<>();
    }

    public static List<Mensagem> carregarMensagens() {
        Mensagem[] array = gson.fromJson(ler(ARQ_MENSAGENS), Mensagem[].class);
        return (array != null) ? new ArrayList<>(Arrays.asList(array)) : new ArrayList<>();
    }

    // ---------- gravação (snapshot completo da coleção em memória) ----------

    public static void salvarUsuarios(Collection<Usuario> usuarios) {
        gravar(ARQ_USUARIOS, gson.toJson(new ArrayList<>(usuarios)));
    }

    public static void salvarDemandas(Collection<Demanda> demandas) {
        gravar(ARQ_DEMANDAS, gson.toJson(new ArrayList<>(demandas)));
    }

    public static void salvarTarefas(Collection<Tarefa> tarefas) {
        gravar(ARQ_TAREFAS, gson.toJson(new ArrayList<>(tarefas)));
    }

    public static void salvarMensagens(Collection<Mensagem> mensagens) {
        gravar(ARQ_MENSAGENS, gson.toJson(new ArrayList<>(mensagens)));
    }

    // ---------- I/O ----------

    private static String ler(String caminho) {
        try {
            return Files.readString(Path.of(caminho));
        } catch (Exception e) {
            return "[]";
        }
    }

    private static void gravar(String caminho, String conteudo) {
        try {
            Files.createDirectories(Path.of(DIR));
            Files.writeString(Path.of(caminho), conteudo);
        } catch (Exception e) {
            System.out.println("ERRO ao gravar " + caminho + ": " + e.getMessage());
        }
    }

    // Leitura no início 
    public static List<Log> carregarLogs() {
        Log[] array = gson.fromJson(ler(ARQ_LOGS), Log[].class);
        return (array != null) ? new ArrayList<>(Arrays.asList(array)) : new ArrayList<>();
    }

    // Gravação sempre que um novo log for gerado
    public static void salvarLogs(Collection<Log> logs) {
        gravar(ARQ_LOGS, gson.toJson(new ArrayList<>(logs)));
    }

}
