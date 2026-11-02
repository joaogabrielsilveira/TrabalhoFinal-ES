public class Usuario {

    private static int ID = 0;
    private final int matricula;
    private String nome;
    private String email;
    private String senha;
    private TipoUsuario type;
    private List<Mensagem> mensagensRecebidas = new ArrayList<>();
    private List<Mensagem> mensagensEnviadas = new ArrayList<>();

    public Usuario(int matricula, String nome, String email, String senha, TipoUsuario tipo) {
        this.matricula = ID;
        ID++;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.type = tipo;
    }

    public boolean equals(Usuario outro) {
        return this.matricula == outro.matricula;
    }

    public void enviarMensagem(String conteudo, Usuario destinatario) {

        Mensagem novaMensagem = new Mensagem(conteudo, this, destinatario);
        this.mensagensEnviadas.add(novaMensagem);
        destinatario.mensagensRecebidas.add(novaMensagem);

    }

     public String getEmail() {
        return this.email;
    }

    public int getMatricula() {
        return this.matricula;
    }

    public String getNome() {
        return this.nome;
    }

    public String getSenha() {
        return this.senha;
    }

    public TipoUsuario getTipo() {
        return this.tipo;
    }

    public void delete() {
        Banco.excluirUsuario(this);
    }
}