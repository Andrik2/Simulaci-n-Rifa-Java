package com.mycompany.simulador_paralelo_rifas_digitales;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Simulacion {
    
    Scanner leer = new Scanner(System.in);
    //Variables de boletos (numero total, apartados, etc)
    private ArrayList<Integer> boletos = new ArrayList<>();
    private String[] apartado;
    private int numBoletos;
    private int numApartados = 0;
     
    //Variable de validez
    private boolean existe;

    //Variable de contador
    private int i;

    //Variables de tiempos
    long tiempoGenerar = 0;
    long tiempoRango = 0;
    long tiempoIndividual = 0;
    long tiempoMostrar = 0;
    
    //metodos
    //Muestra el menu y regresa ya validada la respuesta
    public int menu(){
        int opc;
            do{
                System.out.println("Presione el numero de la accion que desee realizar:\n");
                System.out.println("Menu"); 
                System.out.println("1.- Generar boletos"); 
                System.out.println("2.- Apartar boletos"); 
                System.out.println("3.- Mostrar boletos libres y apartados"); 
                System.out.println("4.- Elegir ganador"); 
                System.out.println("5.- Ver metricas"); 
                System.out.println("6.- Salir");
                opc = leer.nextInt();
                if (opc <1 || opc > 6){
                    System.out.println("Opcion invalida, vuelva a escribir por favor.\n");
                }
            }while(opc < 1 || opc >6);        
        return opc;
    }
    
    //Generará n cantidad de boletos una única vez en paralelo, después no dejará generar más.
    public void generar(){
        
        if (existe){
            System.out.println("Ya existen boletos generados, vuelva a intentarlo mas tarde.\n");
        }
        else{
            do{
            System.out.println("Escriba el numero de boletos que desee generar: ");
            numBoletos = leer.nextInt();
            if (numBoletos <= 0){
                System.out.println("Valor invalido, vuelva a intentarlo.\n");
            }
            }while(numBoletos <= 0);
            
            int hilos = 4; 
            int bloque = numBoletos / hilos;
            Thread[] nucleo = new Thread[hilos];

            boletos.clear();
            
            System.out.println("\nGenerando boletos en paralelo...\n");
            long inicioGenerar = System.currentTimeMillis();
            
            for (i = 0; i < hilos; i++) {

                int numHilo = i+1;
                int inicio = i * bloque + 1;
                int fin;
                if (i == hilos - 1) {
                    fin = numBoletos;     
                } else {
                    fin = inicio + bloque - 1; 
                }

                nucleo[i] = new Thread(() -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {}

                    // Agregar los boletos del bloque
                    synchronized (boletos) {
                        System.out.println("Bloque " + numHilo);
                        for (int j = inicio; j <= fin; j++) {
                            System.out.println("Boleto numero " + j + " agregado");
                            boletos.add(j);
                        }
                    }
                });
                nucleo[i].start();
            }
            // Esperar a que todos los hilos terminen
            for (Thread t : nucleo) {
                try {
                    t.join();
                } catch (InterruptedException e) {}
            }
            long finGenerar = System.currentTimeMillis();
            tiempoGenerar = finGenerar - inicioGenerar;
            
    // Inicializar arreglo de apartados al numero de boletos (evadiendo el 0)
    apartado = new String[numBoletos + 1];
    existe = true;

    System.out.println("Se generaron: " + numBoletos + " boletos con simulacion paralela.\n");
    }
}
    //Apartará los boletos de 2 posibles formas, individualmente o por rango consecutivo
    public void apartar(){
        int bol;
        String nombreAparto;
        int numBoletosApartados;
        int eleccion;
        int inicio;
        int fin;
        int completados;
        int fallos;
        if (existe == false){
            System.out.println("Aun no existen boletos generados, vuelva a intentarlo mas tarde.\n");
            return;
        }
        //Verificación de si  están todos apartados
        if (numApartados == numBoletos){
            System.out.println("Numero de boletos apartados por completo. No se puede apartar mas.\n");
        }
        else{
            System.out.println("Escriba su nombre: ");
            leer.nextLine();
            nombreAparto = leer.nextLine();
            
            do{
                     System.out.println("Si desea apartarlos por rango (1-20, 1-10, 30-60) PRESIONE 1");
                     System.out.println("Si desea apartarlos individualmente (1, 15, 28, 99) PRESIONE 2");
                     eleccion = leer.nextInt();    
                    
             }while(eleccion != 1 && eleccion != 2);
            //Caso de rango
            if (eleccion == 1){
                    do{    
                        System.out.println("Escriba el valor inicial del rango");
                        inicio = leer.nextInt();
                        System.out.println("Escriba el valor final del rango");
                        fin = leer.nextInt();
                        if (inicio < 1 || fin > boletos.size() || inicio > fin) {
                        System.out.println("Rango inválido Vuelva a ingresar.\n"); 
                        }
                    }while(inicio < 1 || fin > boletos.size() || inicio > fin);
                    
                    int totalEnRango = fin - inicio + 1;
                    if(totalEnRango >= (numBoletos-numApartados)){
                        System.out.println("Numero de boletos excedentes a los disponibles.\n");
                        return;
                    }
                    System.out.println("Se apartaran los boletos del " + inicio + " hasta el " + fin);
                    
                    if (totalEnRango <= 10) {
                        fallos = 0;
                        completados = 0;
                    //Si son 10 o menos, no tiene tanto caso hacerlo en paralelo
                    for (int b = inicio; b <= fin; b++) {
                        if (apartarIndividualRango (b, nombreAparto)){
                            completados++;
                        }
                        else{
                            fallos++;
                        }
                    }
                        System.out.println("Se registraron con exito " + completados + " boletos");
                        System.out.println("Hubo " + fallos + " fallos (puede que ya esten apartados esos boletos)");
                    } else {
                    //Si son mas de 10, se harán de forma paralela midiendo el tiempo
                     long inicioRango = System.currentTimeMillis();
                        apartarRangoParalelo(inicio, fin, nombreAparto);
                     long finRango = System.currentTimeMillis();
                      tiempoRango = finRango - inicioRango;
                    }
                }
            //Caso de numeros inidividuales
                else{
                    do{
                        System.out.println("Escriba cuantos boletos desea apartar: ");
                        numBoletosApartados = leer.nextInt();
                        if (numBoletosApartados < 1){
                            System.out.println("Valor de boletos invalido (menor o igual a 0)");
                        }
                    }while (numBoletosApartados < 1); 
                    
                    if (numBoletosApartados >= (numBoletos - numApartados)){
                        System.out.println("El numero de boletos excede los disponibles.");
                        return;
                    }
                    //Si son 10 o menos, lo hará iterativamente
                    if (numBoletosApartados <= 10){
                        for (int b=0; b < numBoletosApartados; b++) {
                        apartarIndividual (nombreAparto);
                        }
                    }
                    //Si son más, los hará en paralelo
                    else{ 
                         ArrayList<Integer> lista = new ArrayList<>();
                        for (i = 0; i < numBoletosApartados; i++){
                            do{
                                System.out.println("Escriba el numero del boleto que desee apartar");
                                bol = leer.nextInt();
                                if (bol < 1 || bol > boletos.size()){
                                    System.out.println("Valor invalido de boleto, vuelva a escribir");
                                }
                                else if (apartado[bol] != null){
                                    System.out.println("Boleto ya apartado. Vuelva a intentarlo");
                                }     
                         }while(bol < 1 || bol > boletos.size() || apartado[bol]!= null);
                         lista.add(bol);
                        }
                        long inicioIndividual = System.currentTimeMillis();
                        apartarIndividualParalelo(lista, nombreAparto);
                        long finIndividual = System.currentTimeMillis();
                        tiempoIndividual = finIndividual - inicioIndividual;
                    }
                }
        }
    }
    
    //Metodo para apartar individualmente 1 solo. Valida el numero y comprueba que no esté apartado
    public void apartarIndividual(String nombre){
        int bol;
            do{
                System.out.println("Escriba el numero del boleto:");
                bol = leer.nextInt();
                if(bol < 1 || bol > boletos.size()){
                    System.out.println("Numero de boleto no valido. Vuelva a intentarlo");
                }else if (apartado[bol] != null){
                    System.out.println("Boleto ya apartado. Vuelva a intentarlo");
            }
            }while (bol < 1 || bol > boletos.size() || apartado[bol] != null);
            apartado[bol] = nombre;
            System.out.println("El boleto " + bol + " fue apartado para " + apartado[bol]);
            numApartados++;
        
    }
    
    //Método para apartar individualmente un rango. Devolviendo un true o un false si puede o no apartarlo
    public boolean apartarIndividualRango(int bol, String nombre){
         if (apartado[bol] != null){
                    System.out.println("Boleto ya apartado por alguien mas. Vuelva a intentarlo");
                    return false;
          }
         else{
            apartado[bol] = nombre;
            System.out.println("El boleto " + bol + " fue apartado para " + apartado[bol]);
            numApartados++;
            return true;
         }
    }
    
    //Metodo para apartar más de 10 boletos no consecutivos de forma paralela
    public void apartarIndividualParalelo(ArrayList<Integer> lista, String nombre){
        int total = lista.size();
        int hilos = 4;

        int bloque = total / hilos;
        Thread[] nucleo = new Thread[hilos];

        System.out.println("Apartando boletos en paralelo\n");   
        
        for (i = 0; i < hilos; i++) {

           int numHilo = i+1;
           int inicio = i * bloque;
           int fin;
           if (i == hilos - 1) {
               fin = total-1;     
           } else {
               fin = inicio + bloque - 1; 
           }

            nucleo[i] = new Thread(() -> {

                System.out.println("Hilo " + (numHilo) + " apartara " + (fin-inicio) + " boletos ");

                for (int j = inicio; j <= fin; j++) {
                    int boleto = lista.get(j);
                    
                    synchronized(apartado){
                        if (apartado[boleto] != null) {
                        System.out.println("El boleto numero " + boleto + " ya esta apartado por alguien mas");
                        }
                        else{
                            apartado[boleto] = nombre;
                            System.out.println("El Hilo " + numHilo + " aparto el boleto " + boleto + " para " + nombre );
                            numApartados++;
                        }
                    }      
                }

            });

            nucleo[i].start();
        }

        // Esperar hilos
        for (Thread t : nucleo) {
            try { t.join(); }
            catch (InterruptedException e) {}
        }   
    }
    
    //Método para apartar un rango de boletos superior a 10 de forma paralela
    public void apartarRangoParalelo(int inicio, int fin, String nombre){
        
        int total = fin - inicio + 1;
        int hilos = 4;

        Thread[] nucleo = new Thread[hilos];
        int bloque = total / hilos;

        System.out.println("Apartando boletos en paralelo (rango)\n");

        for (i = 0; i < hilos; i++) {

            int numHilo = i+1;
            int inicioBloque = i * bloque;
            int finBloque;
            if (i == hilos - 1) {
               finBloque = total-1;     
            } else {
               finBloque = inicioBloque + bloque - 1; 
            }

            nucleo[i] = new Thread(() -> {

                int inicioHilo = inicio + inicioBloque;
                int finHilo = inicio + finBloque;
                System.out.println("Hilo " + (numHilo) + " apartara del boleto " + inicioHilo + " al boleto " + finHilo);
                for (int b = inicioHilo; b <= finHilo; b++) {
                    synchronized(apartado){
                        if (apartado[b] != null) {
                        System.out.println("El boleto numero " + b + " ya esta apartado por alguien mas");
                        }
                        else{
                            apartado[b] = nombre;
                            System.out.println("El Hilo " + numHilo + " aparto el boleto " + b + " para " + nombre);
                            numApartados++;
                        }
                    }   
                }

            });
            nucleo[i].start();
        }

        // Esperar todos los hilos
        for (Thread t : nucleo) {
            try { t.join(); }
            catch (InterruptedException e) {}
        }
    }
    
    //Método para mostrar los boletos y su estado de forma paralela (similar al de generar boletos)
    public void mostrar(){
        if (existe = false){
            System.out.println("Aun no existen boletos generados, vuelva a intentarlo mas tarde.\n");
        }
        else{
            
            int hilos = 4; 
            int bloque = boletos.size() / hilos;
            Thread[] nucleo = new Thread[hilos];

            System.out.println("Mostrando boletos en paralelo...\n");
            long inicioMostrar = System.currentTimeMillis();
            
            for (i = 0; i < hilos; i++) {

                int numHilo = i+1;
                int inicio = i * bloque + 1;
                int fin;
                if (i == hilos - 1) {
                    fin = boletos.size();     
                } else {
                    fin = inicio + bloque - 1; 
                }

                nucleo[i] = new Thread(() -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {}

                    // Agregar los boletos del bloque
                    synchronized (boletos) {
                        System.out.println("Bloque " + numHilo);
                        for (int j = inicio; j <= fin; j++) {
                            if(apartado[j]==null){
                                System.out.println("Boleto " + j + " esta libre");
                           }
                           else{
                            System.out.println("Boleto " + j + " esta apartado por " + apartado[j]);   
                           }
                        }
                    }
                });

                nucleo[i].start();
            }

            // Esperar a que todos los hilos terminen
            for (Thread t : nucleo) {
                try {
                    t.join();
                } catch (InterruptedException e) {}
            }
            long finMostrar = System.currentTimeMillis();
            tiempoMostrar = finMostrar - inicioMostrar;
            
        }
        }
    
    //Método para elegir al ganador, selecciona 1 número al azar y si está apartado lo da a conocer
    public void ganador(){
        if (existe == false){
            System.out.println("Aun no existen boletos generados, vuelva a intentarlo mas tarde.\n");
        }
        else{
            int intentos = 0;
            int ganador;
            do{
                intentos++;
                Random random = new Random();
                ganador = random.nextInt(boletos.size()) + 1; // +1 porque los boletos empiezan en 1    
                if (intentos == numBoletos){
                    System.out.println("No hay ganador aun (no hay boletos apartados)");
                    return;
                }
            }while(apartado[ganador] == null);                    
            System.out.println("El boleto ganador es..." + ganador + "!! a nombre de: " + apartado[ganador]) ;
        }
    }
    
    //Método para mostrar las métricas de tiempos de ejecución de todos los métodos paralelos
    public void metricas(){
        if (existe == false){
            System.out.println("Aun no existen boletos generados, vuelva a intentarlo mas tarde.\n");
        }
        else{
            System.out.println("Tiempos de ejecucion para " + numBoletos +" boletos:");
            System.out.println("Generar " + numBoletos + " boletos: " + tiempoGenerar + " ms");
            System.out.println("Apartar boletos por rango: " + tiempoRango + "ms");
            System.out.println("Apartar boletos individualmente: " + tiempoIndividual + "ms");
            System.out.println("Mostrar boletos: " + tiempoMostrar + "ms");
        }
        
    }
    }
    
