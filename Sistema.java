import java.util.List;

public class Sistema {

    private List<Usuario> u;

    public Sistema() {
    }

    public void criar_usuario(Usuario u, String tipo) {
        //Demais operações

        Banco.salvar_usuario(u);
    }

    public void excluir_usuario(Usuario u) {
        //Demais operações

        Banco.excluir_usuario(u);
    }

    public void login(String email, String senha) {

    }
}
