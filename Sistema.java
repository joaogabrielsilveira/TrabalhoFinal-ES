import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.InputMismatchException;
import java.util.Scanner;


public class Sistema {

    Usuario user = null;
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

    public int lerEscolha(String[] escolhas, Scanner sc) {
        String msg = "Selecione uma opção:";
        int numEscolhas = escolhas.length;
        for (int i = 0; i < numEscolhas; i++) {
            msg += String.format("\n[%d]%s", i+1, escolhas[i]);
        }
        System.out.println(msg);
        int escolha;
        try {
            escolha = sc.nextInt();
        }
        catch (InputMismatchException e) {
            escolha = -1;
            sc.next();
        }
        
        boolean valid = !(escolha > numEscolhas || escolha <= 0);
        if (!valid) {
            System.out.println("Escolha inválida, tente novamente.\n");
        }

        return valid ? escolha : -1;
    }

    public boolean loopLogin(Scanner sc) {
        sc.nextLine();
        System.out.print("Digite o nome de usuário: ");
        String nome = sc.nextLine();
        System.out.print("Digite a senha: ");
        String senha = sc.nextLine();
        boolean sucesso = login(nome, senha);
        if (!sucesso) {
            System.out.println("Usuário ou senha incorretos, tente novamente");
        }
        else {
            System.out.println(String.format("Login realizado com sucesso."));
        }

        return sucesso;
    }

    public void loopSistema() {
        Scanner sc = new Scanner(System.in);
        boolean ativo = true;
        int escolha;
        while (ativo) {
            escolha = lerEscolha(new String[] {"Login", "Sair"}, sc);
            switch (escolha) {
                case 1:
                    if (loopLogin(sc)) {
                        
                    }
                    else {
                        continue;
                    }
                    break;
                case 2:
                    ativo = false;
                    break;
                default:
                    break;
            }
        }
        sc.close();
    }
}
