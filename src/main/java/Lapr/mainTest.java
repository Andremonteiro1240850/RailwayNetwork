package Lapr;

import java.util.List;

public class mainTest {
    public static void main(String[] args) {
        FreightAOD dao = new FreightAOD();
        List<Freight> lista = dao.getAllFreights();

        for (Freight f : lista) {
            System.out.println(f);
        }
    }
}
