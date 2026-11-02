// import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        Usuario rem = new Usuario(
            0,
            "João",
            "joao@gmail.com",
            "joao123",
            TipoUsuario.Cliente
        );
        Usuario dest = new Usuario(
            1, 
            "Lucas",
            "lucas@gmail.com",
            "lucas123",
            TipoUsuario.Desenvolvedor
        );
        
        for (int i = 0; i < 10; i++) {
            rem.enviarMensagem("Mensagem" + i, "Opa cara te mandando uma mensagem numero " + i + " valeu", dest);
        }
        
        Banco.salvarUsuario(rem);
        Banco.salvarUsuario(dest);
        
        Sistema s = new Sistema();
        s.loopSistema();
        return;
    }
}
