package UI;

import Controller.TrolleyController;
import Domain.Trolley;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * The type Trolley ui.
 */
public class TrolleyUI implements Runnable {
    private final TrolleyController trolleyController;
    private final Scanner sc = new Scanner(System.in);

    /**
     * Instantiates a new Trolley ui.
     *
     * @param trolleyController the trolley controller
     */
    public TrolleyUI(TrolleyController trolleyController) {
        this.trolleyController = trolleyController;
    }

    @Override
    public void run() {
        boolean running = true;

        while (running) {
            showMenu();
            System.out.print("Escolha uma opção: ");
            int opcao = getIntInput();

            switch (opcao) {
                case 1:
                    createTrolley();
                    break;
                case 2:
                    startPacking();
                    break;
                case 3:
                    listTrolleys();
                    break;
                case 0:
                    System.out.println("Encerrando aplicação. Até logo!");
                    running = false;
                    break;
                default:
                    System.out.println("Opção inválida! Tente novamente.");
                    break;
            }
        }
    }

    private void showMenu() {
        System.out.println("\n====== MENU TROLLEY ======");
        System.out.println("1. Criar novo trolley");
        System.out.println("2. Executar packing (heurísticas FF, FFD ou BFD)");
        System.out.println("3. Listar trolleys existentes");
        System.out.println("0. Sair");
    }

    private void createTrolley() {
        System.out.print("Digite a capacidade máxima do carrinho: ");
        int capacidade = getIntInput();

        System.out.print("Digite a opção de heurística a usar (FF, FFD, BFD): ");
        String opcao = sc.next().toUpperCase();

        if (!opcao.equals("FF") && !opcao.equals("FFD") && !opcao.equals("BFD")) {
            System.out.println("Heurística inválida! Use FF, FFD ou BFD.");
            return;
        }

        Trolley t = trolleyController.createTrolley(capacidade, opcao);
        System.out.println("Carrinho criado com sucesso!");
        System.out.println(t);
    }

    private void startPacking() {
        if (trolleyController.getTrolley() == null) {
            System.out.println("Nenhum trolley criado. Crie um primeiro.");
            return;
        }

        try {
            System.out.println("Executando packing...");
            trolleyController.packing();
            System.out.println("Packing concluído com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao executar packing: " + e.getMessage());
        }
    }

    private void listTrolleys() {
        List<Trolley> lista = trolleyController.getTrolleyList();
        if (lista.isEmpty()) {
            System.out.println("Nenhum trolley criado ainda.");
            return;
        }

        System.out.println("\n--- Lista de Trolleys ---");
        for (Trolley t : lista) {
            System.out.println(t);
        }
    }

    private int getIntInput() {
        while (true) {
            try {
                return sc.nextInt();
            } catch (InputMismatchException e) {
                System.out.print("Entrada inválida! Digite um número inteiro: ");
                sc.next(); // limpar buffer
            }
        }
    }

    private double getDoubleInput() {
        while (true) {
            try {
                return sc.nextDouble();
            } catch (InputMismatchException e) {
                System.out.print("Entrada inválida! Digite um número: ");
                sc.next(); // limpar buffer
            }
        }
    }
}
