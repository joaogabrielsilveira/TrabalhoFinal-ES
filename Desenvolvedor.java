public class Desenvolvedor extends Usuario {
    
    public Desenvolvedor(int matricula, String nome, String email, String senha, TipoUsuario tipo) {
        super(matricula, nome, email, senha, tipo);
    }
    public Tarefa criar_tarefa() {return null;}
    public void receber_demanda() {}
    public Demanda entregar_demanda() {return null;}

}
