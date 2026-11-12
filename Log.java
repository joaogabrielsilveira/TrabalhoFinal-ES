import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Log {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private LocalDateTime dataHora;
    private String nivel;     // "INFO", "AVISO", "ERRO"
    private String usuario;   // email do usuário que executou a ação (ou "SISTEMA")
    private String acao;      // ex: "LOGIN", "CRIAR_DEMANDA", "ATRIBUIR_DEMANDA", "CRIAR_TAREFA"
    private String detalhe;

    public Log(String nivel, String usuario, String acao, String detalhe) {
        this.dataHora = LocalDateTime.now();
        this.nivel = nivel;
        this.usuario = (usuario != null) ? usuario : "SISTEMA";
        this.acao = acao;
        this.detalhe = detalhe;
    }

    public LocalDateTime getDataHora() { return dataHora; }
    public String getNivel() { return nivel; }
    public String getUsuario() { return usuario; }
    public String getAcao() { return acao; }
    public String getDetalhe() { return detalhe; }

    public void imprimir() {
        System.out.println(this.toString());
    }

    public static void imprimirLista(List<Log> lista) {
        if (lista == null || lista.isEmpty()) {
            System.out.println("Nenhum log encontrado.");
            return;
        }
        for (Log l : lista) {
            l.imprimir();
        }
    }

    // Impressão padronizada
    @Override
    public String toString() {
        return String.format("[%s] [%-5s] [%s] [%s] %s",
            dataHora.format(formatter),
            nivel,
            usuario,
            acao,
            detalhe
        );
    }
}