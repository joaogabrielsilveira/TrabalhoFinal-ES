import com.google.gson.annotations.SerializedName;

public enum TipoUsuario {
    @SerializedName("Cliente")
    Cliente,
    @SerializedName("Avaliador")
    Avaliador,
    @SerializedName("Desenvolvedor")
    Desenvolvedor
}