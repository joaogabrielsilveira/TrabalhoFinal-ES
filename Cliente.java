
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Cliente extends Usuario {
    
    List<Demanda> demandasSolicitadas;

    public Cliente(int matricula, String nome, String email, String senha, TipoUsuario tipo) {
        super(matricula, nome, email, senha, tipo);
        this.demandasSolicitadas = new ArrayList<>();
    }

    public Demanda solicitar_demanda() {
        System.out.println("Digite o título da demanda: ");
        String titulo = Sistema.sc.nextLine();
        System.out.println("Digite a descrição da demanda: ");
        String descricao = Sistema.sc.nextLine();
        System.out.println("Digite o prazo da demanda (em dias): ");
        int prazo = Sistema.sc.nextInt();

        Demanda novaDemanda = new Demanda(this, titulo, descricao, LocalDateTime.now().plusDays(prazo));

        demandasSolicitadas.add(novaDemanda);

        return novaDemanda;
    }
    public void consultar_demandas() {
        System.out.println("As demandas solicitadas são: ");
        for (Demanda demanda : demandasSolicitadas) {
            System.out.println("ID: " + demanda.getId() + ", Título: " + demanda.getTitulo() + ", Situação: " + demanda.getSituacao());
        }

        System.out.println("Digite o ID da demanda que deseja consultar: ");
        
        int ID = Sistema.sc.nextInt();

        Demanda demandaSelecionada = demandasSolicitadas.stream().filter(d -> d.getId() == ID).findFirst().orElse(null);
        System.out.println("Titulo da demanda: " + demandaSelecionada.getTitulo() +
         "\nConteúdo da demanda: " + demandaSelecionada.getDescricao() +
          "\nPrazo da demanda: " + demandaSelecionada.getPrazo() +
           " dias\nPrioridade da demanda: " + demandaSelecionada.getPrioridade());

    }

    public Demanda atualizarDemanda() {
        System.out.println("As demandas solicitadas são: ");
        for (Demanda demanda : demandasSolicitadas) {
            System.out.println("ID: " + demanda.getId() + ", Título: " + demanda.getTitulo() + ", Situação: " + demanda.getSituacao());
        }

        System.out.println("Digite o ID da demanda que deseja atualizar: ");
        int ID = Sistema.sc.nextInt();

        Demanda demandaSelecionada = demandasSolicitadas.stream().filter(d -> d.getId() == ID).findFirst().orElse(null);
        System.out.println("Digite o novo conteúdo da demanda: ");

        String novoConteudo = Sistema.sc.nextLine();
        System.out.println("Digite o novo prazo da demanda (em dias): ");
        int novoPrazo = Sistema.sc.nextInt();

        demandaSelecionada.editarDados(novoConteudo, LocalDateTime.now().plusDays(novoPrazo));
        return demandaSelecionada;
    }

}
