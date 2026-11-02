public class Avaliador extends Usuario {
    
    public Avaliador(int matricula, String nome, String email, String senha, TipoUsuario tipo) {
        super(matricula, nome, email, senha, tipo);
    }
    public void analisar_demanda() {}
    public void encaminhar_demanda() {}
    public void editar_demanda() {}
    public void cancelar_demanda() {}

}
