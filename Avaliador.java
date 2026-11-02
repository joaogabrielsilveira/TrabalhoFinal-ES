
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Avaliador extends Usuario {
    
    List<Demanda> demandasParaAprovacao;
    List<Demanda> demandasAprovadas;

    public Avaliador(int matricula, String nome, String email, String senha, TipoUsuario tipo) {
        super(matricula, nome, email, senha, tipo);
        this.demandasParaAprovacao = new ArrayList<>();
        this.demandasAprovadas = new ArrayList<>();
    }

    public void atualizarPrioridade(Demanda demanda) {
        if (demanda.getPrazo() <= 14) {
            demanda.setPrioridade(PrioridadeDemanda.Alta);
        } else if (demanda.getPrazo() > 14 && demanda.getPrazo() <= 30) {
            demanda.setPrioridade(PrioridadeDemanda.Média);
        } else if (demanda.getPrazo() > 30) {
            demanda.setPrioridade(PrioridadeDemanda.Baixa);
        }
    }

    public void setarPrioridade(Demanda demanda, PrioridadeDemanda prioridade) {
        demanda.setPrioridade(prioridade);
    }

    public void retornar_demanda(Demanda demandaRetorno) {
        System.out.println("Digite o motivo do retorno da demanda: ");
        String motivoRetorno = Sistema.sc.nextLine();
        demandaRetorno.setSituacao(StatusDemanda.Retornada);
        enviarMensagem("Demanda retornada", "Sua demanda foi retornada para ajustes. Motivo: " + motivoRetorno, this);
    }

    public void avaliar_demanda() {
        if (demandasParaAprovacao.isEmpty()) {
            System.out.println("Não há demandas para avaliação.");
            return;
        }

        System.out.print("As demandas para aprovação são: ");
        for (Demanda demanda : demandasParaAprovacao) {
            System.out.print(demanda.getId() + ", ");
        }
        System.out.println();
        System.out.println("Digite o ID da demanda que deseja avaliar: ");
        int idDemanda = Sistema.sc.nextInt();
        Demanda demandaSelecionada = null;
        demandaSelecionada = demandasParaAprovacao.stream().filter(d -> d.getId() == idDemanda).findFirst().orElse(null);
        System.out.println("Titulo da demanda: " + demandaSelecionada.getTitulo() +
         "\nConteúdo da demanda: " + demandaSelecionada.getDescricao() +
          "\nPrazo da demanda: " + demandaSelecionada.getPrazo() +
           " dias\nPrioridade da demanda: " + demandaSelecionada.getPrioridade());

        System.out.println("Digite 1 para aprovar a demanda ou 2 para retornar a demanda: ");
        int escolha = Sistema.sc.nextInt();
        while (escolha != 1 && escolha != 2) {
            System.out.println("Escolha inválida, tente novamente.");
            escolha = Sistema.sc.nextInt();
        }
            if (escolha == 1) {
                demandaSelecionada.setSituacao(StatusDemanda.Aprovada);
                System.out.println("Demanda aprovada com sucesso.");
                encaminharDemanda(demandaSelecionada);

        }   else if (escolha == 2) {
                retornar_demanda(demandaSelecionada);
                System.out.println("Demanda retornada com sucesso.");
        }
    }
    
    public void encaminharDemanda(Demanda demandaEncaminhada) {

        PriorityQueue<Desenvolvedor> minHeapDesenvolvedoresDisponiveis = new PriorityQueue<>((d1, d2) -> Integer.compare(d1.getDemandasAtribuidas().size(), d2.getDemandasAtribuidas().size()));
        System.out.print("As demandas para aprovação são: ");
        for (Demanda demanda : demandasParaAprovacao) {
            System.out.print(demanda.getId() + ", ");
        }
        System.out.println();
        System.out.println("Digite o ID da demanda que deseja avaliar: ");
        int idDemanda = Sistema.sc.nextInt();
        Demanda demandaSelecionada = null;
        demandaSelecionada = demandasParaAprovacao.stream().filter(d -> d.getId() == idDemanda).findFirst().orElse(null);

        for (Desenvolvedor desenvolvedor : Sistema.getDesenvolvedores()) {
            minHeapDesenvolvedoresDisponiveis.offer(desenvolvedor);
        }

        Desenvolvedor desenvolvedorSelecionado = minHeapDesenvolvedoresDisponiveis.poll();
        desenvolvedorSelecionado.receberDemanda(demandaSelecionada);
        System.out.println("Demanda encaminhada para o desenvolvedor: " + desenvolvedorSelecionado.getNome());
        enviarMensagem("Demanda encaminhada", "Sua demanda foi encaminhada para o desenvolvedor: " + desenvolvedorSelecionado.getNome(), this);
    }
}