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
            TipoUsuario.Cliente
        );
        Usuario ava = new Usuario(
            2,
            "Pedro",
            "pedro@gmail.com",
            "pedro23",
            TipoUsuario.Cliente
        );
        Usuario dev = new Usuario(
            3, 
            "Tiago",
            "tiago@gmail.com",
            "tiago123",
            TipoUsuario.Cliente
        );
        
        for (int i = 0; i < 10; i++) {
            rem.enviarMensagem("Mensagem" + (i+1), "Opa cara te mandando uma mensagem numero " + (i+1) + " valeu", dest);
        }
        
        Banco.salvarUsuario(rem);
        Banco.salvarUsuario(dest);
        Banco.salvarUsuario(ava);
        Banco.salvarUsuario(dev);
        
        Sistema s = new Sistema();
        s.loopSistema();
        return;
    }
}
