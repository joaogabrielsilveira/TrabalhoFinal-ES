import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Sistema {

    Usuario user = null;
    public static Scanner sc = new Scanner(System.in);
    public static void limparConsole() {
    try {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } else {
            System.out.print("\033[H\033[2J");   // código ANSI: cursor no topo + limpa a tela
            System.out.flush();
        }
    } catch (Exception e) {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
    public static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    // ---- Fonte única da verdade: cada entidade existe UMA vez, aqui em memória ----
    private static final Map<String, Usuario> users = new LinkedHashMap<>();      // chave: email
    private static final Map<Integer, Demanda> demandas = new LinkedHashMap<>();  // chave: id
    private static final Map<Integer, Tarefa> tarefas = new LinkedHashMap<>();    // chave: id
    private static final List<Mensagem> mensagens = new ArrayList<>();
    private static final List<Log> logs = new ArrayList<>();

    /** Lê o disco UMA vez. Depois disso, tudo vive em memória e só é gravado. */
    public Sistema() {
        users.clear();
        demandas.clear();
        tarefas.clear();
        mensagens.clear();

        for (Usuario u : Banco.carregarUsuarios()) users.put(u.getEmail(), u);
        for (Demanda d : Banco.carregarDemandas()) demandas.put(d.getId(), d);
        for (Tarefa t : Banco.carregarTarefas()) tarefas.put(t.getId(), t);
        mensagens.addAll(Banco.carregarMensagens());

        // Contadores estáticos voltam de onde pararam (senão os IDs colidem a cada execução)
        Usuario.restaurarContador(users.values().stream().mapToInt(Usuario::getMatricula).max().orElse(-1) + 1);
        Demanda.ID = demandas.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1) + 1;
        Tarefa.ID = tarefas.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        Mensagem.ID = mensagens.stream().mapToInt(Mensagem::getId).max().orElse(-1) + 1;

        logs.clear();
        logs.addAll(Banco.carregarLogs());
    }

    // ---------------- Usuários ----------------

    public static Usuario getUsuario(String email) {
        if (email == null) return null;
        return users.get(email);
    }

    public boolean registrarUsuario(Usuario u) {
        if (users.containsKey(u.getEmail())) {
            return false;
        }
        users.put(u.getEmail(), u);
        salvarUsuarios();
        registrarLog("INFO", u.getEmail(), "REGISTRO", 
            String.format("Conta criada para %s (%s) como %s.", u.getNome(), u.getEmail(), u.getTipo()));
        return true;
    }

    public static void excluirUsuario(Usuario u) {
        // Demais operações (demandas, mensagens...) ficam como TODO, como no original
        users.remove(u.getEmail());
        salvarUsuarios();
        registrarLog("AVISO", u.getEmail(), "EXCLUIR_USUARIO", 
            String.format("Usuário %s (%s) excluído do sistema.", u.getNome(), u.getEmail()));
    }

    /** Mantido por compatibilidade: o objeto já está na memória, só precisa gravar. */
    public static void atualizarUsuario(Usuario u) {
        salvarUsuarios();
    }

    // ---------------- Demandas / Tarefas / Mensagens ----------------

    public static void registrarDemanda(Demanda d) {
        demandas.put(d.getId(), d);
        salvarDemandas();
        registrarLog("INFO", d.getEmailSolicitante(), "CRIAR_DEMANDA", 
            String.format("Demanda '%s' [ID %d] criada com prazo de %d dias.", d.getTitulo(), d.getId(), d.getPrazo()));
    }

    public static Demanda getDemanda(int id) {
        return demandas.get(id);
    }

    public static void registrarTarefa(Tarefa t) {
        tarefas.put(t.getId(), t);
        salvarTarefas();
        registrarLog("INFO", "SISTEMA", "CRIAR_TAREFA", 
            String.format("Tarefa '%s' [ID %d] vinculada à demanda ID %d.", t.getTitulo(), t.getId(), t.getDemandaVinculada()));
    }

    public static List<Tarefa> getTarefas() {
        return new ArrayList<>(tarefas.values());
    }

    public static List<Tarefa> getTarefasDaDemanda(int idDemanda) {
        return tarefas.values().stream()
            .filter(t -> t.getDemandaVinculada() == idDemanda)
            .collect(Collectors.toList());
    }

    public static void removerTarefasDaDemanda(int idDemanda) {
        tarefas.values().removeIf(t -> t.getDemandaVinculada() == idDemanda);
        salvarTarefas();
    }

    public static void registrarMensagem(Mensagem m) {
        mensagens.add(m);
        salvarMensagens();
        registrarLog("INFO", m.getEmailRemetente(), "ENVIAR_MENSAGEM", 
            String.format("Mensagem enviada para %s. Assunto: '%s'.", m.getEmailDestinatario(), m.getTitulo()));
    }

    /** Mais recentes primeiro. */
    public static List<Mensagem> getMensagensRecebidas(String email) {
        List<Mensagem> r = new ArrayList<>();
        for (int i = mensagens.size() - 1; i >= 0; i--) {
            if (mensagens.get(i).getEmailDestinatario().equals(email)) r.add(mensagens.get(i));
        }
        return r;
    }

    public static List<Mensagem> getMensagensEnviadas(String email) {
        List<Mensagem> r = new ArrayList<>();
        for (int i = mensagens.size() - 1; i >= 0; i--) {
            if (mensagens.get(i).getEmailRemetente().equals(email)) r.add(mensagens.get(i));
        }
        return r;
    }

    // ---------------- Persistência (só grava; nunca recarrega) ----------------

    public static void salvarUsuarios()  { Banco.salvarUsuarios(users.values()); }
    public static void salvarDemandas()  { Banco.salvarDemandas(demandas.values()); }
    public static void salvarTarefas()   { Banco.salvarTarefas(tarefas.values()); }
    public static void salvarMensagens() { Banco.salvarMensagens(mensagens); }
    public static void salvarLogs() { Banco.salvarLogs(logs); }

    public static void registrarLog(String nivel, String usuario, String acao, String detalhe) {
        Log novo = new Log(nivel, usuario, acao, detalhe);
        logs.add(novo);
        salvarLogs();
    }

    // Consulta (mais recentes primeiro)
    public static List<Log> getLogs() {
        List<Log> r = new ArrayList<>(logs);
        java.util.Collections.reverse(r);
        return r;
    }

    // Consultar log por nível
    public static List<Log> consultarLogsPorNivel(String nivel) {
        List<Log> r = new ArrayList<>();
        if (nivel == null) return r;
        // Percorre do mais recente para o mais antigo
        for (int i = logs.size() - 1; i >= 0; i--) {
            Log l = logs.get(i);
            if (l.getNivel().equalsIgnoreCase(nivel.trim())) {
                    r.add(l);
            }
        }
        return r;
    }

    // Consultar logs por usuário (pelo email)
    public static List<Log> consultarLogsPorUsuario(String email) {
        List<Log> r = new ArrayList<>();
        if (email == null) return r;
        for (int i = logs.size() - 1; i >= 0; i--) {
            Log l = logs.get(i);
            if (l.getUsuario().equalsIgnoreCase(email.trim())) {
                r.add(l);
            }
        }
        return r;
    }

    // Consultar por tipo de ação
    public static List<Log> consultarLogsPorAcao(String acao) {
        List<Log> r = new ArrayList<>();
        if (acao == null) return r;
        for (int i = logs.size() - 1; i >= 0; i--) {
            Log l = logs.get(i);
            if (l.getAcao().equalsIgnoreCase(acao.trim())) {
                r.add(l);
            }
        }
        return r;
    }

    public void loopConsultaLogs() {
        boolean ativo = true;
        while (ativo) {
            int escolha = lerEscolha(new String[]{
                "Ver todos os logs",
                "Filtrar por nível (INFO / AVISO)",
                "Filtrar por usuário",
                "Filtrar por ação (ex: LOGIN, CRIAR_DEMANDA)",
                "Voltar"
            });

            switch (escolha) {
                case 1:
                    Log.imprimirLista(getLogs());
                    break;
                case 2:
                    System.out.print("Digite o nível desejado (INFO ou AVISO): ");
                    Log.imprimirLista(consultarLogsPorNivel(sc.nextLine()));
                    break;
                case 3:
                    System.out.print("Digite o email do usuário: ");
                    Log.imprimirLista(consultarLogsPorUsuario(sc.nextLine()));
                    break;
                case 4:
                    System.out.print("Digite o nome da ação: ");
                    Log.imprimirLista(consultarLogsPorAcao(sc.nextLine()));
                    break;
                default:
                    ativo = false;
                    break;
            }

            if (ativo) {
                System.out.println("\nPressione Enter para continuar...");
                sc.nextLine();
                limparConsole();
            }
        }
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

        if (valid) limparConsole();

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
        Usuario novo;
        switch (escolhaTipo) {
            case 1:
                novo = new Cliente(0, nome, email, senha, TipoUsuario.Cliente);
                break;
            case 2:
                novo = new Avaliador(0, nome, email, senha, TipoUsuario.Avaliador);
                break;
            default:
                novo = new Desenvolvedor(0, nome, email, senha, TipoUsuario.Desenvolvedor);
                break;
        }

        registrarUsuario(novo);
        System.out.println(String.format("Conta registrada com sucesso para o usuário %s", nome));
        registrarLog("INFO", email, "REGISTRO", 
            String.format("Conta registrada com sucesso para o usuário %s (%s).", nome, novo.getTipo()));
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
            registrarLog("AVISO", email, "LOGIN", "Tentativa de login com credenciais incorretas.");
        }
        else {
            user = getUsuario(email);
            System.out.println(String.format("Login do usuário %s realizado com sucesso.", user.getNome()));
            registrarLog("INFO", email, "LOGIN", String.format("Login do usuário %s realizado com sucesso.", user.getNome()));
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

            if (itens.length == 0) {
                System.out.println("Nenhuma opção disponível.");
                return;
            }
            
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
                    m.setLida();
                    salvarMensagens(); // getConteudo() marcou a mensagem como lida
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
        // System.out.print("Digite o email do destinatário: ");


        // String emailDest = sc.nextLine();
        int listLength = 5;
        List<Usuario> destinatarios = new ArrayList<>();
        switch (user.getTipo()) {
            case Cliente:
                loopPaginacaoLista(
                    () -> {
                        destinatarios.clear();
                        for (Demanda d : ((Cliente)user).getDemandasSolicitadas()) {
                            if (d.getSituacao() == StatusDemanda.EmAndamento && getUsuario(d.getEmailDesenvolvedor()) != null)
                                destinatarios.add(getUsuario(d.getEmailDesenvolvedor()));
                        }

                        return destinatarios.toArray(new Usuario[0]);
                    },
                    listLength, 
                    u -> u.getNome() + " [" + u.getEmail() + "]",
                    u -> {
                        System.out.print("Digite o título da mensagem: ");
                        String titulo = sc.nextLine();
                        System.out.println("Digite o conteúdo da mensagem: ");
                        String conteudo = sc.nextLine();
                        user.enviarMensagem(titulo, conteudo, u);
                    }
                );
                break;
            case Desenvolvedor:
                break;
            case Avaliador:
                break;
            default:
                break;
        }
        
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
        Sistema.sc.nextLine();

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
                    Sistema.sc.nextLine();
                    if (novoPrazo < 0 || novoPrazo < d.getPrazo()) {
                        System.out.println("Prazo inválido, deve ser maior que o atual.");
                    } else {
                        ((Cliente)user).atualizarDemanda(d, novoConteudo, novoPrazo);
                        System.out.println("Demanda atualizada com sucesso.");
                        registrarLog("INFO", user.getEmail(), "EDITAR_DEMANDA", 
                            String.format("Demanda '%s' [ID %d] atualizada com nova descrição e prazo de %d dias.", d.getTitulo(), d.getId(), d.getPrazo()));
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
                    if (dev == null) {
                        System.out.println("Nenhum desenvolvedor cadastrado para receber a demanda.");
                    } else {
                        System.out.println("Demanda encaminhada para o desenvolvedor: " + dev.getNome());
                        registrarLog("INFO", user.getEmail(), "ENCAMINHAR_DEMANDA", 
                            String.format("Demanda '%s' [ID %d] encaminhada para o dev %s.", d.getTitulo(), d.getId(), dev.getNome()));
                    }
                }
                else if (escolhaAcao == 2) {
                    System.out.println("Digite o motivo do retorno da demanda: ");
                    String motivoRetorno = Sistema.sc.nextLine();
                    ((Avaliador)user).retornarDemanda(d, motivoRetorno);
                    registrarLog("AVISO", user.getEmail(), "RETORNAR_DEMANDA", 
                        String.format("Demanda '%s' [ID %d] retornada com motivo: %s.", d.getTitulo(), d.getId(), motivoRetorno));
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
                int escolhaEdicao = lerEscolha(new String[]{"Editar", "Concluir", "Voltar"});
                if (escolhaEdicao == 1) {
                    System.out.print("Digite o novo conteúdo da tarefa: ");
                    String novoConteudo = sc.nextLine();
                
                    ((Desenvolvedor)user).atualizarTarefa(t, novoConteudo);
                    System.out.println("Tarefa atualizada com sucesso.");
                    registrarLog("INFO", user.getEmail(), "EDITAR_TAREFA", 
                    String.format("Tarefa '%s' [ID %d] da demanda %d atualizada com nova descrição.", t.getTitulo(), t.getId(), t.getDemandaVinculada()));
                }
                else if (escolhaEdicao == 2) {
                    ((Desenvolvedor)user).finalizarTarefa(t);
                    registrarLog("INFO", user.getEmail(), "CONCLUIR_TAREFA", 
                        String.format("Tarefa '%s' [ID %d] marcada como concluída.", t.getTitulo(), t.getId()));
                    System.out.println("Tarefa finalizada com sucesso.");
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
                        registrarLog("INFO", user.getEmail(), "FINALIZAR_DEMANDA", 
                            String.format("Demanda '%s' [ID %d] finalizada e entregue pelo desenvolvedor.", d.getTitulo(), d.getId()));
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
        user = getUsuario(user.getEmail());
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
                registrarLog("INFO", user.getEmail(), "LOGOUT", String.format("Usuário %s deslogou do sistema.", user.getNome()));
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
            escolha = lerEscolha(new String[] {"Login", "Criar conta", "Consultar Logs do Sistema", "Sair"});
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
                    loopConsultaLogs();
                    break;
                case 4:
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
        return users.values().stream()
            .filter(u -> u.getTipo() == TipoUsuario.Desenvolvedor)
            .map(u -> (Desenvolvedor) u)
            .collect(Collectors.toList());
    }

    public static List<Cliente> getClientes() {
        return users.values().stream()
            .filter(u -> u.getTipo() == TipoUsuario.Cliente)
            .map(u -> (Cliente) u)
            .collect(Collectors.toList());
    }

    public static List<Avaliador> getAvaliadores() {
        return users.values().stream()
            .filter(u -> u.getTipo() == TipoUsuario.Avaliador)
            .map(u -> (Avaliador) u)
            .collect(Collectors.toList());
    }
}
