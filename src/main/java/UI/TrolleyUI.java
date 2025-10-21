package UI;

import Controller.TrolleyController;
import Domain.Trolley;

import java.util.Scanner;

public class TrolleyUI implements Runnable {
    TrolleyController trolleyController;
    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Digite a capacidade máxima do carrinho: ");
        int capacidade = sc.nextInt();
        System.out.println("Digite a opção de heurística a usar (FF, FFD, BFD): ");
        String opcao = sc.next();
        trolleyController.createTrolley(capacidade, opcao);
        System.out.println("Carrinho criado com sucesso: " + trolleyController.getTrolley().toString());
    }
}
