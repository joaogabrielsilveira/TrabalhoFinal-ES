public class Usuario {

    private int matricula;
    private String nome;
    private String email;
    private String senha;
    private TipoUsuario tipo;

    public Usuario(int matricula, String nome, String email, String senha, TipoUsuario tipo) {
        this.matricula = matricula;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipo = tipo;
    }

    public void enviar_mensagem(String conteudo, Usuario destinatario) {

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



}