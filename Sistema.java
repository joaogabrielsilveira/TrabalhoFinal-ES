import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Sistema {

    private List<Usuario> u;

    public Sistema() {
    }

    public void registrar_usuario(Usuario u) {

        Banco.salvar_usuario(u);
    }

    public void excluir_usuario(Usuario u) {
        //Demais operações

        Banco.excluir_usuario(u);
    }

    public boolean login(String email, String senha) {
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean ok = false;

        if(u.stream().anyMatch(usuario -> usuario.getEmail().equals(email))) {
            
            String hash = u.stream()
                .filter(usuario -> usuario.getEmail().equals(email))
                .findFirst()
                .get()
                .getSenha();
            
            ok = encoder.matches("senha", hash);
        }

        return ok;
    }
}
