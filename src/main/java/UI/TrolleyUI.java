package UI;

import Controller.TrolleyController;
import Domain.Trolley;

import java.util.Scanner;

public class TrolleyUI implements Runnable {
    TrolleyController trolleyController;
    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Type the maximum capacity of the trolley: ");
        int capacidade = sc.nextInt();
        System.out.println("Type the heuristic option to use (FF, FFD, BFD): ");
        String opcao = sc.next();
        trolleyController.createTrolley(capacidade, opcao);
        System.out.println("Trolley created with success: " + trolleyController.getTrolley().toString());
    }
}
