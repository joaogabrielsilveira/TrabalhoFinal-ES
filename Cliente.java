public class Cliente extends Usuario {
    
    public Cliente(int matricula, String nome, String email, String senha, TipoUsuario tipo) {
        super(matricula, nome, email, senha, tipo);
    }
    public Demanda solicitar_demanda() {return null;}
    public Demanda[] consultar_demandas() {return null;}

}
