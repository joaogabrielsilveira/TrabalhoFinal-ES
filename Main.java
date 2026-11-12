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

        s.loopSistema();
    }
}
