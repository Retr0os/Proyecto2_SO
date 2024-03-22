/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package classes;


import interfaz.ColaUi;
import interfaz.GlobalUi;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InteligenciaArtificial extends Thread {

    private Administrador administrador;
    Personaje personajebenson;
    Personaje personajeToph;
    
    int simulationTime;
    Random random = new Random();
    public Semaphore mutex;
    
    public String[] winners = new String[500];

    private int winnersPointer = 0;
    public int bensonWins = 0;
    public int TophWins = 0;
    

    public InteligenciaArtificial() {
//        this.administrador = Main.sistemaOperativo;
        this.mutex = Main.mutex;
        // 10 segundos por defecto
        this.simulationTime = 10 * 1000;
    }

    @Override
    public void run() {
        try {
            while (true) {   
                
                long auxTime = simulationTime;
                this.mutex.acquire();
                
                if(this.personajebenson == null || this.personajeToph == null){
                    Main.sistemaOperativo.regresarpersonajesAColas(personajebenson, personajeToph);
                    Thread.sleep(auxTime);
                    
                } else{
                    // Decidiendo
                    System.out.println("estoy decidiendo");
                    GlobalUi.getMainPage().getStatusLabel().setText("Decidiendo");
                    //tomar un numero random para determinar ganador. 0=benson, 1=toph
//                    int num = random.nextInt(2);
//                    Personaje winner = (num == 0) ? this.personajebenson : this.personajeToph;
                    Personaje winner = this.decideWinner();

                    Thread.sleep((long) (auxTime * 0.3));
                    
                    // entero para determinar resultado de la simulacion
                    int decision = random.nextInt(100);
                    
                    if(decision <= 40){ //hay ganador
                        System.out.println("Gano alguien");
                        GlobalUi.getMainPage().getStatusLabel().setText("Hay un ganador");
                        String car = winner.getMarca();
                        winners[winnersPointer] = car + "-" + Integer.toString(winner.getId());
                        if(winnersPointer < winners.length){
                            winnersPointer++;
                        }           
                        
                        GlobalUi.getMainPage().getWinnersLabel().setText(printWinners());
                        if(winner.getMarca().equals("Benson")){ // es benson
                            this.bensonWins++;
                            //sumar contador de combates ganadas
                            GlobalUi.getMainPage().getbensonWinsLabel().setText(Integer.toString(bensonWins));
                            //decir que gano
                            GlobalUi.getMainPage().getbensonWinnerLabel().setText("Ganador!");
                            
                        }else if(winner.getMarca().equals("Toph")){ //es toph                            
                            this.TophWins++;
                            GlobalUi.getMainPage().gettophWinsLabel().setText(Integer.toString(TophWins));
                            GlobalUi.getMainPage().gettophWinnerLabel().setText("Ganador!");
                        }
                        
                        System.out.println("benson ganadas: " + bensonWins);
                        System.out.println("toph ganadas: " + TophWins);
                        Thread.sleep((long) (auxTime * 0.5));
                        
                    }else if(decision <= 67){ //hay empate
                        System.out.println("hubo empate");
                        GlobalUi.getMainPage().getStatusLabel().setText("Hubo Empate");
                        Thread.sleep((long) (auxTime * 0.5));
                        Main.sistemaOperativo.regresarPersonajeCola1(personajebenson);
                        Main.sistemaOperativo.regresarPersonajeCola1(personajeToph);                        
                    }else{ //van a refuerzo
                        System.out.println("nos vamos a refuerzo");
                        GlobalUi.getMainPage().getStatusLabel().setText("Vamos a refuerzo");
                        Thread.sleep((long) (auxTime * 0.5));
                        //enviar a la cola de refuerzo
                        Main.sistemaOperativo.enviarpersonajesColaRefuerzo(this.personajebenson, this.personajeToph);
                    }                    
                    System.out.println("Esperando");
                    GlobalUi.getMainPage().getStatusLabel().setText("Esperando");
                }
                this.administrador.updateColasUi();
                System.out.println(this.administrador.tophColaNivel1.print());
                Thread.sleep((long) (auxTime * 0.2));
                
                GlobalUi.getMainPage().getbensonWinnerLabel().setText("");
                GlobalUi.getMainPage().gettophWinnerLabel().setText("");
                
                this.mutex.release();
                Thread.sleep(500);                
            }                                   
        } catch (InterruptedException ex) {
            Logger.getLogger(InteligenciaArtificial.class.getName()).log(Level.SEVERE, null, ex);
        }
    }   
    
    private String printWinners(){
        String text = "";
        for(int i = 0; i < winnersPointer; i++){
            String value = winners[i];
            text += value + ", ";
        }
        return text;
    }
    
    public void setPersonajeBenson(Personaje nuevobenson){
        this.personajebenson = nuevobenson;
    }
    
    public void setPersonajeToph(Personaje nuevoBg){
        this.personajeToph = nuevoBg;
    }
    
    public void setSimulationTime(int timeSec){
        this.simulationTime = timeSec * 1000;
    }
    
    public void setAdministrador(){
        this.administrador = Main.sistemaOperativo;
    }
    
    public Personaje decideWinner(){
        
        Personaje winner = null;
        
        // si la diferencia de poder de fuerza es <= 50, entonces gana el de mejor vida, si es la misma, gana el que tenga mas defensa
        if(Math.abs(personajebenson.getFuerza() - personajeToph.getFuerza()) <= 50){
            
            if (this.personajeToph.getBonus() < this.personajebenson.getBonus()){
                winner = this.personajeToph; 
            }else if(this.personajebenson.getBonus() < this.personajeToph.getBonus()){
                winner = this.personajebenson;
            }else if(this.personajeToph.getBonus() == this.personajebenson.getBonus()){ //misma defensa
                winner = this.winnerByHP();
            }
            
            }else { // la diferencia es mayor a 50 dmg, gana el que tenga mas vida
            winner = this.winnerByHP();
        }
        
        return winner;   
    }
    
    public Personaje winnerByHP(){
        Personaje winner = null;
        
        if(personajebenson.getFuerza() > personajeToph.getFuerza()){
            //benson tiene mas HP
            winner = this.personajebenson;
        }else if (personajebenson.getFuerza() < personajeToph.getFuerza()){
            winner = this.personajeToph;
        }else{ // tienen los mismos ataque, se hace al azar
            int num = random.nextInt(2);
            winner = (num == 0) ? this.personajebenson : this.personajeToph;
        }
        
        return winner;
    }
    
}   

