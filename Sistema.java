import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.Consumer;

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
        Sistema.d = new ArrayList<>();
        Sistema.c = new ArrayList<>();
        Sistema.a = new ArrayList<>();
    }

    public static Usuario getUsuario(String email) {
        if (users == null) {
            return null;
        }
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
        switch (u.getTipo()) {
            case Cliente:
                c.add((Cliente)u);
                break;
            case Avaliador:
                a.add((Avaliador)u);
                break;
            case Desenvolvedor:
                d.add((Desenvolvedor)u);
                break;
            default:
                break;
        }
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

        Usuario novo = new Usuario(0, nome, email, senha, tipo);
        switch (escolhaTipo) {
            case 1:
                tipo = TipoUsuario.Cliente;
                novo = new Cliente(0, nome, email, senha, tipo);
            case 2:
                tipo = TipoUsuario.Avaliador;
                novo = new Desenvolvedor(0, nome, email, senha, tipo);
            case 3:
                tipo = TipoUsuario.Desenvolvedor;
                novo = new Avaliador(0, nome, email, senha, tipo);
            default:
                break;
        }

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
            user = getUsuario(email);
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

    public <T> void loopPaginacaoLista(Supplier<T[]> fornecedorItens, int listLength, Function<T, String> conversorString, Consumer<T> acaoAoVisualizar) {
        boolean ativo = true;
        int page = 0;
        while (ativo) {
            // Agora a lista é recarregada a cada iteração do menu!
            T[] itens = fornecedorItens.get(); 
            
            int numItens = itens.length;
            int start = page * listLength;
            int end = Math.min(start + listLength, numItens);
            
            List<String> opcoes = new ArrayList<>();
            for (int i = start; i < end; i++) {
                opcoes.add(conversorString.apply(itens[i]));
            }
            
            boolean hasNextPage = end < numItens;
            boolean hasPreviousPage = page > 0;
            
            if (hasNextPage) opcoes.add("Próxima página");
            if (hasPreviousPage) opcoes.add("Página Anterior");
            opcoes.add("Voltar");
            
            int escolhaMsg = lerEscolha(opcoes.toArray(new String[0]));
            if (escolhaMsg == opcoes.size() || escolhaMsg == -1) {
                ativo = false;
            } else if (escolhaMsg != -1) {
                if (escolhaMsg <= (end - start)) {
                    T itemSelecionado = itens[start + escolhaMsg - 1];
                    acaoAoVisualizar.accept(itemSelecionado);
                } else {
                    String opcaoArray = opcoes.get(escolhaMsg - 1);
                    if (opcaoArray.equals("Próxima página")) {
                        page++;
                    } else if (opcaoArray.equals("Página Anterior")) {
                        page--;
                    }
                }
            }
        }
    }

    public void loopMensagensLista(int escolha, int listLength) {
        boolean recebida = (escolha == 1);
        
        loopPaginacaoLista(
            // 1. Supplier para buscar a caixa de entrada ou saída atualizada
            () -> {
                if (escolha == 1) {
                    return user.getMensagensRecebidas().toArray(new Mensagem[0]);
                } else {
                    return user.getMensagensEnviadas().toArray(new Mensagem[0]);
                }
            },
            
            listLength, 
            
            // 2. Function
            m -> getMessageString(m, false, recebida), 
            
            // 3. Consumer
            m -> {
                System.out.println(getMessageString(m, true, false));
                if (escolha == 1) {
                    int escolhaResposta = lerEscolha(new String[]{"Responder", "Voltar"});
                    if (escolhaResposta == 1) {
                        Usuario destUser = getUsuario(m.getEmailRemetente());
                        if (destUser == null) {
                            System.out.println("Remetente não existe mais na base de dados.");
                        } else {
                            System.out.println("Digite o conteúdo da resposta: ");
                            String conteudo = Sistema.sc.nextLine(); // Certifique-se de usar Sistema.sc
                            user.enviarMensagem("Re: " + m.getTitulo(), conteudo, destUser);
                        }   
                    }
                } else {
                    System.out.println("[Visualizando a mensagem. Digite enter para voltar]");
                    Sistema.sc.nextLine();
                }
            }
        );
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

    public void loopDemandasCliente() {
        int listLength = 5;
        
        loopPaginacaoLista(
            // 1. Supplier: ensina como pegar as demandas sempre atualizadas
            () -> ((Cliente)user).getDemandasSolicitadas().toArray(new Demanda[0]), 
            
            listLength, 
            
            // 2. Function: como exibir a string na lista
            d -> getDemandaString(d, false), 
            
            // 3. Consumer: o que fazer ao clicar
            d -> {
                System.out.println(getDemandaString(d, true));
                int escolhaEdicao = lerEscolha(new String[]{"Editar", "Voltar"});
                if (escolhaEdicao == 1) {
                    System.out.println("Digite o novo conteúdo da demanda: ");
                    String novoConteudo = Sistema.sc.nextLine();
                    System.out.println("Digite o novo prazo da demanda (em dias): ");
                    int novoPrazo = Sistema.sc.nextInt();
                    if (novoPrazo < 0 || novoPrazo < d.getPrazo()) {
                        System.out.println("Prazo inválido, deve ser maior que o atual.");
                    } else {
                        ((Cliente)user).atualizarDemanda(d, novoConteudo, novoPrazo);
                        System.out.println("Demanda atualizada com sucesso.");
                    }
                }
            }
        );
    }

    public void loopDemandasAvaliador() {
        int listLength = 5;
        
        loopPaginacaoLista(
            () -> ((Avaliador)user).getDemandasParaAprovacao().toArray(new Demanda[0]), 
            
            listLength, 
            
            d -> getDemandaString(d, false), 
            
            d -> {
                System.out.println(getDemandaString(d, true));
                int escolhaAcao = lerEscolha(new String[]{"Aprovar", "Reprovar", "Voltar"});
                if (escolhaAcao == 1) {
                    Desenvolvedor dev = ((Avaliador)user).encaminharDemanda(d);
                    System.out.println("Demanda encaminhada para o desenvolvedor: " + dev.getNome());
                }
                else if (escolhaAcao == 2) {
                    System.out.println("Digite o motivo do retorno da demanda: ");
                    String motivoRetorno = Sistema.sc.nextLine();
                    ((Avaliador)user).retornar_demanda(d, motivoRetorno);
                }
            }
        );
    }

    public void loopTarefasDesenvolvedor(int idDemanda) {
        int listLength = 5;
        
        loopPaginacaoLista(
            () -> {
                if (idDemanda < 0) {
                    return ((Desenvolvedor)user).getTarefasAtribuidas().toArray(new Tarefa[0]);
                } else {
                    return ((Desenvolvedor)user).getTarefasAtribuidas().stream()
                        .filter(x -> x.getDemandaVinculada() == idDemanda)
                        .toArray(Tarefa[]::new);
                }
            },

            listLength, 
            
            t -> t.toString(), 
            
            t -> {
                System.out.println();
                int escolhaEdicao = lerEscolha(new String[]{"Editar", "Voltar"});
                if (escolhaEdicao == 1) {
                    System.out.println("Digite o novo conteúdo da tarefa: ");
                    String novoConteudo = sc.nextLine();
                
                    ((Desenvolvedor)user).atualizarTarefa(t, novoConteudo);
                    System.out.println("Tarefa atualizada com sucesso.");
                }
            }
        );
    }

    public void loopDemandasDesenvolvedor() {
        int listLength = 5;
        
        loopPaginacaoLista(
            () -> ((Desenvolvedor)user).getDemandasAtribuidas().toArray(new Demanda[0]), 
            
            listLength, 
            
            d -> getDemandaString(d, false), 
            
            d -> {
                System.out.println(getDemandaString(d, true));
                int escolhaAcao = lerEscolha(new String[]{"Criar tarefa", "Gerenciar tarefas", "Entregar demanda", "Voltar"});
                if (escolhaAcao == 1) {
                    System.out.print("Digite o título da tarefa: ");
                    String titulo = Sistema.sc.nextLine();
                    System.out.print("Digite a descrição da tarefa: ");
                    String descricao = Sistema.sc.nextLine();

                    ((Desenvolvedor)user).criarTarefa(titulo, descricao, d);
                }
                else if (escolhaAcao == 2) {
                    loopTarefasDesenvolvedor(d.getId());
                }
                else if (escolhaAcao == 3) {
                    System.out.println("Digite 'SIM' para confimar o envio da demanda: ");
                    String confirmacao = sc.nextLine();
                    if (confirmacao.equals("SIM")) {
                        ((Desenvolvedor)user).entregarDemanda(d);
                        System.out.println("Demanda entregue com sucesso.");
                    }
                    else {
                        System.out.println("Confirmação não sucedida.");
                    }
                }
            }
        );
    }

    public void loopUsuario() {
        boolean ativo = true;
        String[] opcoes = new String[]{};
        switch (user.getTipo()) {
            case Cliente:
                opcoes = new String[]{"Criar Demanda", "Ver demandas", "Gerenciar mensagens", "Logout"};
                break;
            case Avaliador:
                opcoes = new String[]{"Ver demandas", "Gerenciar mensagens", "Logout"};
                break;
            case Desenvolvedor:
                opcoes = new String[]{"Ver demandas", "Ver tarefas", "Gerenciar mensagens", "Logout"};
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
                    case Cliente:
                        if (escolha == 1) {
                            loopCriarDemanda();
                        }
                        else if (escolha == 2) {
                            loopDemandasCliente();
                        }
                        else if (escolha == 3) {
                            loopMensagens();
                        }
                        break;
                    case Avaliador:
                        if (escolha == 1) {
                            loopDemandasAvaliador();
                        }
                        else if (escolha == 2) {
                            loopMensagens();
                        }
                        break;
                    case Desenvolvedor:
                        if (escolha == 1) {
                            loopDemandasDesenvolvedor();
                        }
                        else if (escolha == 2) {
                            loopTarefasDesenvolvedor(-1);
                        }
                        else if (escolha == 3) {
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
