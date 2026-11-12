public class Main {
    public static void main(String[] args) {
        // O Sistema carrega o banco uma única vez; só depois criamos/consultamos usuários.
        Sistema s = new Sistema();

        // Dados de exemplo: só são criados se ainda não existirem (evita duplicar a cada execução)
        if (Sistema.getUsuario("joao@gmail.com") == null) {
            Usuario rem = new Cliente(0, "João", "joao@gmail.com", "joao123", TipoUsuario.Cliente);
            Usuario dest = new Cliente(1, "Lucas", "lucas@gmail.com", "lucas123", TipoUsuario.Cliente);
            Usuario ava = new Avaliador(2, "Pedro", "pedro@gmail.com", "pedro123", TipoUsuario.Avaliador);
            Usuario dev = new Desenvolvedor(3, "Tiago", "tiago@gmail.com", "tiago123", TipoUsuario.Desenvolvedor);

            s.registrarUsuario(rem);
            s.registrarUsuario(dest);
            s.registrarUsuario(ava);
            s.registrarUsuario(dev);

            rem.enviarMensagem("Mensagem1", "Opa cara te mandando uma mensagem numero 1 valeu", dest);
        }

        // Se a lista de logs estiver vazia, gera um histórico completo para testes:
        if (Sistema.getLogs().isEmpty()) {
                Sistema.registrarLog("INFO", "joao@gmail.com", "REGISTRO", "Conta criada para João como Cliente.");
                Sistema.registrarLog("INFO", "pedro@gmail.com", "REGISTRO", "Conta criada para Pedro como Avaliador.");
                Sistema.registrarLog("INFO", "tiago@gmail.com", "REGISTRO", "Conta criada para Tiago como Desenvolvedor.");
                Sistema.registrarLog("AVISO", "joao@gmail.com", "LOGIN", "Tentativa de login com credenciais incorretas.");
                Sistema.registrarLog("INFO", "joao@gmail.com", "LOGIN", "Login do usuário João realizado com sucesso.");
                Sistema.registrarLog("INFO", "joao@gmail.com", "CRIAR_DEMANDA", "Demanda 'Novo App Mobile' [ID 0] criada com prazo de 10 dias.");
                Sistema.registrarLog("INFO", "pedro@gmail.com", "ENCAMINHAR_DEMANDA", "Demanda 'Novo App Mobile' [ID 0] encaminhada para o dev Tiago.");
                Sistema.registrarLog("INFO", "tiago@gmail.com", "CRIAR_TAREFA", "Tarefa 'Modelar banco' [ID 1] vinculada à demanda ID 0.");
                Sistema.registrarLog("INFO", "tiago@gmail.com", "CONCLUIR_TAREFA", "Tarefa 'Modelar banco' [ID 1] marcada como concluída.");
                Sistema.registrarLog("INFO", "joao@gmail.com", "LOGOUT", "Usuário João deslogou do sistema.");
            }

        s.loopSistema();
    }
}
