import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.InputMismatchException;
import java.util.Scanner;



public class Sistema {

    Usuario user = null;
    public static Scanner sc = new Scanner(System.in);
    private List<Usuario> u;
    private static List<Desenvolvedor> d;
    private static List<Cliente> c;
    private static List<Avaliador> a;
    public Sistema() {
        u = Banco.recuperar_usuarios();
    }

    public void registrarUsuario(Usuario u) {

        Banco.salvarUsuario(u);
    }

    public void excluirUsuario(Usuario u) {
        //Demais operações

        Banco.excluirUsuario(u);
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
            
            ok = encoder.matches(senha, hash);
        }

        return ok;
    }

    public int lerEscolha(String[] escolhas) {
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

        sc.nextLine();

        return valid ? escolha : -1;
    }

    public boolean loopLogin() {
        System.out.print("Digite o nome de usuário: ");
        String email = sc.nextLine();
        System.out.print("Digite a senha: ");
        String senha = sc.nextLine();
        boolean sucesso = login(email, senha);
        if (!sucesso) {
            System.out.println("Usuário ou senha incorretos, tente novamente");
        }
        else {
            Usuario logged = u.stream()
                .filter(usuario -> usuario.getEmail().equals(email))
                .findFirst()
                .get();
            user = logged;
            System.out.println(String.format("Login do usuário %s realizado com sucesso.", user.getNome()));
        }

        return sucesso;
    }

    public void loopUsuario() {
        boolean ativo = true;
        String[] opcoes = new String[]{};
        switch (user.getTipo()) {
            case Avaliador:
                opcoes = new String[]{"Criar Demanda", "Ver demandas", "Ver mensagens", "Logout"};
                break;
            case Cliente:
                opcoes = new String[]{"Criar Demanda", "Ver demandas", "Ver mensagens", "Logout"};
                break;
            case Desenvolvedor:
                opcoes = new String[]{"Criar Demanda", "Ver demandas", "Ver mensagens", "Logout"};
                break;
            default:
                break;
        }  
        while (ativo) {
            int escolha;
            escolha = lerEscolha(opcoes);
            if (escolha == opcoes.length) {
                System.out.println(String.format("Usuário %s deslogado com sucesso", user.getNome()));
                user = null;
                ativo = false;
            }
            switch (user.getTipo()) {
                case Avaliador:
                    break;
                case Cliente:
                    break;
                case Desenvolvedor:
                    break;
                default:
                    break;

            }   
        }
    }

    public void loopSistema() {
        boolean ativo = true;
        int escolha;
        while (ativo) {
            escolha = lerEscolha(new String[] {"Login", "Sair"});
            switch (escolha) {
                case 1:
                    if (loopLogin()) {
                        loopUsuario();
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
        System.out.println("Saindo do sistema");
        sc.close();
    }

    public static List<Desenvolvedor> getDesenvolvedores() {
        return d;
    }

    public static List<Cliente> getClientes() {
        return c;
    }

    public static List<Avaliador> getAvaliadores() {
        return a;
    }
}
