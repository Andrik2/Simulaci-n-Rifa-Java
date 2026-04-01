package com.mycompany.simulador_paralelo_rifas_digitales;

public class Simulador_Paralelo_Rifas_Digitales {
    public static void main(String[] args) {
        
        int opc;
        Simulacion sim = new Simulacion();
        
        do{
            opc = sim.menu();    
            switch(opc){
                case 1:
                    sim.generar();
                break;
                case 2:
                    sim.apartar();
                break;
                case 3:
                    sim.mostrar();
                break;
                case 4:
                    sim.ganador();
                break;
                case 5:
                    sim.metricas();
                break;
                case 6:
                    System.out.println("Gracias por usar el programa.");
                break;
                default:
        
            }
        }while (opc != 6);
    }
}
