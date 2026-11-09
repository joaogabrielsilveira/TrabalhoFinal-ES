import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;


public class Sistema {

    Usuario user = null;
    public static Scanner sc = new Scanner(System.in);
    public static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static List<Usuario> users;
    private static List<Desenvolvedor> d;
    private static List<Cliente> c;
    private static List<Avaliador> a;
    public Sistema() {
        Sistema.users = Banco.recuperarUsuarios();
    }

    public static Usuario getUsuario(String email) {
        Optional<Usuario> query = 
        Sistema.users.stream()
        .filter(usuario -> usuario.getEmail().equals(email))
        .findFirst();

        if (!query.isPresent()) {
            return null;
        }

        return query.get();
    }

    public void registrarUsuario(Usuario u) {

        Banco.salvarUsuario(u);
        Sistema.users = Banco.recuperarUsuarios();
    }

    public void excluirUsuario(Usuario u) {
        //Demais operações

        Banco.excluirUsuario(u);
        Sistema.users = Banco.recuperarUsuarios();
    }

    public static void atualizarUsuario(Usuario u) {
        Banco.excluirUsuario(u);
        Banco.salvarUsuario(u);
        Sistema.users = Banco.recuperarUsuarios();
    }

    public boolean login(String email, String senha) {
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean ok = false;

        if(getUsuario(email) != null) {
            
            String hash = getUsuario(email).getSenha();
            
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

    public boolean loopRegistro() {
        System.out.print("Digite o nome de usuário: ");
        String nome = sc.nextLine();
        System.out.print("Digite o email de usuário: ");
        String email = sc.nextLine();
        Usuario existente = getUsuario(email);
        if (existente != null) {
            System.out.println("Usuário com esse email já existe. Não foi possível registrar.");
            return false;
        }

        System.out.print("Digite a senha: ");
        String senha = sc.nextLine();

        System.out.println("Escolha o tipo de conta: ");
        int escolhaTipo = -1;
        while ((escolhaTipo = lerEscolha(new String[]{"Cliente", "Avaliador", "Desenvolvedor"})) < 0);
        TipoUsuario tipo = TipoUsuario.Cliente;
        switch (escolhaTipo) {
            case 1:
                tipo = TipoUsuario.Cliente;
            case 2:
                tipo = TipoUsuario.Avaliador;
            case 3:
                tipo = TipoUsuario.Desenvolvedor;
            default:
                break;
        }

        Usuario novo = new Usuario(0, nome, email, senha, tipo);
        registrarUsuario(novo);
        System.out.println(String.format("Conta registrada com sucesso para o usuário %s", nome));
        return true;
    }

    public boolean loopLogin() {
        System.out.print("Digite o email de usuário: ");
        String email = sc.nextLine();
        System.out.print("Digite a senha: ");
        String senha = sc.nextLine();
        boolean sucesso = login(email, senha);
        if (!sucesso) {
            System.out.println("Usuário ou senha incorretos, tente novamente");
        }
        else {
            Usuario logged = getUsuario(email);
            switch (logged.getTipo()) {
                case TipoUsuario.Cliente:
                    logged = new Cliente(logged.getMatricula(), senha, email, senha, TipoUsuario.Cliente);
                    break;
                case TipoUsuario.Avaliador:
                    logged = new Avaliador(logged.getMatricula(), senha, email, senha, TipoUsuario.Avaliador);
                    break;
                case TipoUsuario.Desenvolvedor:
                    logged = new Desenvolvedor(logged.getMatricula(), senha, email, senha, TipoUsuario.Desenvolvedor);
                    break;
                default:
                    System.out.println("Tipo de usuário inválido");
                    break;
            }
            user = logged;
            System.out.println(String.format("Login do usuário %s realizado com sucesso.", user.getNome()));
        }

        return sucesso;
    }

    public String getMessageString(Mensagem msg, boolean conteudo, boolean recebida) {
        String txt = "";
        if (recebida) {
            if (msg.isLida()) {
                txt = "(Lida)\n";
            }
            else {
                txt = "(Não Lida)\n";
            }
        }
        txt += String.format("Data: %s\nRemtente: %s [%s]\nDestinatário: %s [%s]\nAssunto: %s\n", msg.getData().format(dateFormatter), msg.getNomeRemetente(), msg.getEmailRemetente(), msg.getNomeDestinatario(), msg.getEmailDestinatario(), msg.getTitulo());
        if (conteudo) {
            txt += String.format("Conteúdo:\n%s", msg.getConteudo());
        }
        return txt;
    }

    public void loopMensagensLista(int escolha, int listLength) {
        boolean ativo = true;
        int page = 0;
        while (ativo) {
            Mensagem[] mensagens = new Mensagem[]{};
            if (escolha == 1) {
                mensagens = user.getMensagensRecebidas().toArray(new Mensagem[0]);
            }
            else if (escolha == 2) {
                mensagens = user.getMensagensEnviadas().toArray(new Mensagem[0]);
            }
            int numMensagens = mensagens.length;
            int start = page * listLength;
            int end = Math.min(start+listLength, numMensagens);

            boolean recebida = (escolha == 1);
            
            List<String> opcoesMsg = new ArrayList<>();
            for (int i = start; i < end; i++) {
                opcoesMsg.add(getMessageString(mensagens[i], false, recebida));
            }
            
            boolean hasNextPage, hasPreviousPage;
            hasNextPage = end < numMensagens;
            hasPreviousPage = page > 0;
            
            if (hasNextPage) {
                opcoesMsg.add("Próxima página");
            }
            if (hasPreviousPage) {
                opcoesMsg.add("Página Anterior");
            }
            opcoesMsg.add("Voltar");
            

            int escolhaMsg = lerEscolha(opcoesMsg.toArray(new String[0]));
            if (escolhaMsg == opcoesMsg.size() || escolhaMsg == -1) {
                ativo = false;
            }
            else if (escolhaMsg != -1) {
                if (escolhaMsg <= (end - start)) {
                    Mensagem m = mensagens[start + escolhaMsg-1];
                    System.out.println(getMessageString(m, true, false));
                    if (escolha == 1) {
                        int escolhaResposta = lerEscolha(new String[]{"Responder", "Voltar"});
                        if (escolhaResposta == 1) {
                            Usuario destUser = getUsuario(m.getEmailRemetente());
                            if (destUser == null) {
                                System.out.println("Remetente não existe mais na base de dados.");
                            }
                            else {
                                System.out.println("Digite o conteúdo da resposta: ");
                                String conteudo = sc.nextLine();
                                user.enviarMensagem("Re: " + m.getTitulo(), conteudo, destUser);
                            }   
                        }
                    }
                    else {
                        System.out.println("[Visualizando a mensagem. Digite enter para voltar]");
                        sc.nextLine();
                    }
                }
                else {
                    String[] opcoesArray = opcoesMsg.toArray(new String[0]);
                    if (opcoesArray[escolhaMsg-1].contains("Próxima")) {
                        page++;
                    }
                    else if (opcoesArray[escolhaMsg-1].contains("Anterior")){
                        page--;
                    }
                }
            }
        }
    }

    public boolean loopMensagensEscrever() {
        System.out.print("Digite o email do destinatário: ");
        String emailDest = sc.nextLine();
        Usuario dest = getUsuario(emailDest);
        if (dest == null) {
            System.out.println("Email não encontrado.");
            return false;
        }
        System.out.print("Digite o título da mensagem: ");
        String titulo = sc.nextLine();
        System.out.println("Digite o conteúdo da mensagem: ");
        String conteudo = sc.nextLine();
        user.enviarMensagem(titulo, conteudo, dest);
        return true;
    }

    public void loopMensagens() {
        int listLength = 5;
        boolean ativo = true;
        while(ativo) {
            int escolha = lerEscolha(new String[]{"Ver caixa de entrada", "Ver caixa de saída", "Escrever mensagem", "Sair"});
            if (escolha == 1 || escolha == 2) {
                loopMensagensLista(escolha, listLength);
            }
            else if (escolha == 3) {
                loopMensagensEscrever();
            }
            else if (escolha == 4) {
                ativo = false;
            }
        }
    }

    public void loopCriarDemanda() {
        System.out.println("Digite o título da demanda: ");
        String titulo = Sistema.sc.nextLine();
        System.out.println("Digite a descrição da demanda: ");
        String descricao = Sistema.sc.nextLine();
        System.out.println("Digite o prazo da demanda (em dias): ");
        int prazo = Sistema.sc.nextInt();

        Demanda d = ((Cliente)user).solicitarDemanda(titulo, descricao, prazo);
        System.out.println("Demanda criada com sucesso, ID: " + d.getId());
    }

    public String getDemandaString(Demanda d, boolean conteudo) {
        String txt = String.format("[%s] Demanda %d - %s", d.getSituacao(), d.getId(), d.getTitulo());
        txt += String.format("\nPrazo: %d dias", d.getPrazo());
        if (conteudo) txt += String.format("\nDescrição: %s", d.getDescricao());

        return txt;
    }

    public void loopDemandasLista() {
        boolean ativo = true;
        int page = 0;
        int listLength = 5;
        while (ativo) {
            Demanda[] demandas = ((Cliente)user).getDemandasSolicitadas().toArray(new Demanda[0]);
            
            int numMensagens = demandas.length;
            int start = page * listLength;
            int end = Math.min(start+listLength, numMensagens);
            
            List<String> opcoesDemanda = new ArrayList<>();
            for (int i = start; i < end; i++) {
                opcoesDemanda.add(getDemandaString(demandas[i], false));
            }
            
            boolean hasNextPage, hasPreviousPage;
            hasNextPage = end < numMensagens;
            hasPreviousPage = page > 0;
            
            if (hasNextPage) {
                opcoesDemanda.add("Próxima página");
            }
            if (hasPreviousPage) {
                opcoesDemanda.add("Página Anterior");
            }
            opcoesDemanda.add("Voltar");
            

            int escolhaMsg = lerEscolha(opcoesDemanda.toArray(new String[0]));
            if (escolhaMsg == opcoesDemanda.size() || escolhaMsg == -1) {
                ativo = false;
            }
            else if (escolhaMsg != -1) {
                if (escolhaMsg <= (end - start)) {
                    Demanda d = demandas[start + escolhaMsg-1];
                    System.out.println(getDemandaString(d, true));
                    int escolhaEdicao = lerEscolha(new String[]{"Editar", "Voltar"});
                    if (escolhaEdicao == 1) {
                        System.out.println("Digite o novo conteúdo da demanda: ");
                        String novoConteudo = Sistema.sc.nextLine();
                        System.out.println("Digite o novo prazo da demanda (em dias): ");
                        int novoPrazo = Sistema.sc.nextInt();
                        if (novoPrazo < 0 || novoPrazo < d.getPrazo()) {
                            System.out.println("Prazo inválido, deve ser maior que o atual.");
                        }
                        else {
                            ((Cliente)user).atualizarDemanda(d, novoConteudo, novoPrazo);
                            System.out.println("Demanda atualizada com sucesso.");
                        }
                    }
                }
                else {
                    String[] opcoesArray = opcoesDemanda.toArray(new String[0]);
                    if (opcoesArray[escolhaMsg-1].contains("Próxima")) {
                        page++;
                    }
                    else if (opcoesArray[escolhaMsg-1].contains("Anterior")){
                        page--;
                    }
                }
            }
        }
    }

    public void loopUsuario() {
        boolean ativo = true;
        String[] opcoes = new String[]{};
        switch (user.getTipo()) {
            case Avaliador:
                opcoes = new String[]{"Criar Demanda", "Ver demandas", "Gerenciar mensagens", "Logout"};
                break;
            case Cliente:
                opcoes = new String[]{"Criar Demanda", "Ver demandas", "Gerenciar mensagens", "Logout"};
                break;
            case Desenvolvedor:
                opcoes = new String[]{"Criar Demanda", "Ver demandas", "Gerenciar mensagens", "Logout"};
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
            if (user != null) {
                switch (user.getTipo()) {
                    case Avaliador:
                        if (escolha == 3) {
                            loopMensagens();
                        }
                        break;
                    case Cliente:
                        if (escolha == 3) {
                            loopMensagens();
                        }
                        else if (escolha == 1) {
                            loopCriarDemanda();
                        }
                        else if (escolha == 2) {
                            loopDemandasLista();
                        }
                        break;
                    case Desenvolvedor:
                        if (escolha == 3) {
                            loopMensagens();
                        }
                        break;
                    default:
                        break;
                }   
            }   
        }
    }

    public void loopSistema() {
        boolean ativo = true;
        int escolha;
        while (ativo) {
            escolha = lerEscolha(new String[] {"Login", "Criar conta", "Sair"});
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
                    loopRegistro();
                    break;
                case 3:
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
