// import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        Usuario rem = new Cliente(
            0,
            "João",
            "joao@gmail.com",
            "joao123",
            TipoUsuario.Cliente
        );
        Usuario dest = new Cliente(
            1, 
            "Lucas",
            "lucas@gmail.com",
            "lucas123",
            TipoUsuario.Cliente
        );
        Usuario ava = new Avaliador(
            2,
            "Pedro",
            "pedro@gmail.com",
            "pedro123",
            TipoUsuario.Avaliador
        );
        Usuario dev = new Desenvolvedor(
            3, 
            "Tiago",
            "tiago@gmail.com",
            "tiago123",
            TipoUsuario.Desenvolvedor
        );
        
        for (int i = 0; i < 1; i++) {
            rem.enviarMensagem("Mensagem" + (i+1), "Opa cara te mandando uma mensagem numero " + (i+1) + " valeu", dest);
        }
        Sistema s = new Sistema();
        
        s.registrarUsuario(rem);
        s.registrarUsuario(dest);
        s.registrarUsuario(ava);
        s.registrarUsuario(dev);
        s.loopSistema();
        return;
    }
}
