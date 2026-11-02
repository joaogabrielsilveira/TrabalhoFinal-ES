import com.google.gson.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Banco {

    private static Gson gson = new GsonBuilder()
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

    public static void salvarUsuario(Usuario novo) {
        Usuario[] array = gson.fromJson(ler("database/usuarios.json"), Usuario[].class);
        List<Usuario> lista = (array != null) ? new ArrayList<>(Arrays.asList(array)) : new ArrayList<>();
        lista.add(novo);
        gravar("database/usuarios.json", gson.toJson(lista));
    }

    public static void excluirUsuario(Usuario u) {
        Usuario[] array = gson.fromJson(ler("database/usuarios.json"), Usuario[].class);
        List<Usuario> lista = (array != null) ? new ArrayList<>(Arrays.asList(array)) : new ArrayList<>();
        lista.removeIf(usuario -> usuario.getEmail().equals(u.getEmail()));
        gravar("database/usuarios.json", gson.toJson(lista));
    }

    public static List<Usuario> recuperarUsuarios() {
        Usuario[] array = gson.fromJson(ler("database/usuarios.json"), Usuario[].class);
        return (array != null) ? new ArrayList<>(Arrays.asList(array)) : new ArrayList<>();
    }

    public static void armazenarDemanda(Demanda d) {
        Demanda[] array = gson.fromJson(ler("database/demandas.json"), Demanda[].class);
        List<Demanda> lista = (array != null) ? new ArrayList<>(Arrays.asList(array)) : new ArrayList<>();
        lista.add(d);
        gravar("database/demandas.json", gson.toJson(lista));
    }

    public static List<Demanda> recuperarDemandas() {
        Demanda[] array = gson.fromJson(ler("database/demandas.json"), Demanda[].class);
        return (array != null) ? new ArrayList<>(Arrays.asList(array)) : new ArrayList<>();
    }

    public static void excluirDemanda(Demanda d) {
        List<Demanda> lista = recuperarDemandas();
        lista.removeIf(demanda -> demanda.getTitulo().equals(d.getTitulo()));
        gravar("database/demandas.json", gson.toJson(lista));
    }

    public static void editarDemanda(Demanda dAtualizada) {
        List<Demanda> lista = recuperarDemandas();
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getTitulo().equals(dAtualizada.getTitulo())) {
                lista.set(i, dAtualizada);
                break;
            }
        }
        gravar("database/demandas.json", gson.toJson(lista));
    }

    public static void armazenarTarefa(Tarefa t) {
            Tarefa[] array = gson.fromJson(ler("database/tarefas.json"), Tarefa[].class);
            List<Tarefa> lista = (array != null) ? new ArrayList<>(Arrays.asList(array)) : new ArrayList<>();
            lista.add(t);
            gravar("database/tarefas.json", gson.toJson(lista));
        }

    public static void armazenarMensagem(Mensagem m) {
        Mensagem[] array = gson.fromJson(ler("database/mensagems.json"), Mensagem[].class);
        List<Mensagem> lista = (array != null) ? new ArrayList<>(Arrays.asList(array)) : new ArrayList<>();
        lista.add(m);
        gravar("database/mensagems.json", gson.toJson(lista));
    }

    public static List<Mensagem> recuperarMensagens() {
        Mensagem[] array = gson.fromJson(ler("database/mensagems.json"), Mensagem[].class);
        return (array != null) ? new ArrayList<>(Arrays.asList(array)) : new ArrayList<>();
    }

    private static String ler(String caminho) {
        try {
            return Files.readString(Path.of(caminho));
        } catch (Exception e) {
            return "[]";
        }
    }

    private static void gravar(String caminho, String conteudo) {
        try {
            Files.writeString(Path.of(caminho), conteudo);
        } catch (Exception e) {}
    }
}
