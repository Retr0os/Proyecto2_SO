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

public class Administrador extends Thread {
    int counter = 0;
    boolean running = true;
    
    public InteligenciaArtificial ia;
    public Semaphore mutex;
    
    public Cola bensonColaNivel1;
    public Cola bensonColaNivel2;
    public Cola bensonColaNivel3;
    public Cola tophColaNivel1;
    public Cola tophColaNivel2;
    public Cola tophColaNivel3;
    public Cola bensonRefuerzo;
    public Cola tophRefuerzo;
    
    public ColaUi colaBensonUi1;
    public ColaUi colaBensonUi2;
    public ColaUi colaBensonUi3;
    public ColaUi colaBensonUiRef;
    
    public ColaUi colaTophUi1;
    public ColaUi colaTophUi2;
    public ColaUi colaTophUi3;
    public ColaUi colaTophUiRef;
    
    final Random porcentaje = new Random();
    
    public Administrador(){
        creacionColas();
        this.mutex = Main.mutex;
        for (int i = 0; i < 10; i++) {
            this.createPersonajesIniciales("Benson");
            this.createPersonajesIniciales("Toph");
        }
        updateColasUi();
        System.out.println("cola numero 1 de Toph: " + this.tophColaNivel1.print());
        System.out.println("cola numero 2 de Toph: " + this.tophColaNivel2.print());
        System.out.println("cola numero 3 de Toph: " + this.tophColaNivel3.print());
        System.out.println("cola numero 1 de Benson: " + this.bensonColaNivel1.print());
        System.out.println("cola numero 2 de Benson: " + this.bensonColaNivel2.print());
        System.out.println("cola numero 3 de Benson: " + this.bensonColaNivel3.print());
        this.ia = Main.ia;
    }

    private void creacionColas() {
        this.tophColaNivel1 = new Cola(); 
        this.tophColaNivel2 = new Cola(); 
        this.tophColaNivel3 = new Cola(); 
        this.bensonColaNivel1 = new Cola();
        this.bensonColaNivel2 = new Cola();
        this.bensonColaNivel3 = new Cola();
        this.bensonRefuerzo = new Cola();
        this.tophRefuerzo = new Cola();
        
        this.colaBensonUi1 = GlobalUi.getMainPage().getcolaBensonUi1();
        this.colaBensonUi2 = GlobalUi.getMainPage().getcolaBensonUi2();
        this.colaBensonUi3 = GlobalUi.getMainPage().getcolaBensonUi3();
        this.colaBensonUiRef = GlobalUi.getMainPage().getcolaBensonUiRef();
        
        this.colaTophUi1 = GlobalUi.getMainPage().getcolaTophUi1();
        this.colaTophUi2 = GlobalUi.getMainPage().getcolaTophUi2();
        this.colaTophUi3 = GlobalUi.getMainPage().getcolaTophUi3();
        this.colaTophUiRef = GlobalUi.getMainPage().getcolaTophUiRef();
    }
    
    @Override
    public void run(){
        try {
            while(this.running){
                this.mutex.acquire();
                this.setCounter(this.counter + 1);
                
                this.desencolarRefuerzoPersonaje(bensonRefuerzo);
                this.desencolarRefuerzoPersonaje(tophRefuerzo);
                
                if(this.counter >= 2){
                    int result = porcentaje.nextInt(100);

                    this.addPersonaje("Benson", result);
                    this.addPersonaje("Toph", result);
                    this.setCounter(0);
                }   

                this.sumarContadorCambiarPrioridad(bensonColaNivel2);
                this.sumarContadorCambiarPrioridad(bensonColaNivel3);
                this.sumarContadorCambiarPrioridad(tophColaNivel2);
                this.sumarContadorCambiarPrioridad(tophColaNivel3);
                
                updateColasUi();

                Personaje benson = this.getPersonajeColas(bensonColaNivel1, bensonColaNivel2, bensonColaNivel3);
                Personaje toph = this.getPersonajeColas(tophColaNivel1, tophColaNivel2, tophColaNivel3);
                
                ia.setPersonajeBenson(benson);
                ia.setPersonajeToph(toph);
                
                //actualizar ids y personajes en interfaz
                GlobalUi.getMainPage().getUiBensonId().setText(("Benson-" + benson.getId()));
                GlobalUi.getMainPage().getUiTophId().setText(("Toph-" + toph.getId()));
                GlobalUi.getMainPage().setPersonajesImgsUi();
                // actualizar HP y calidad en interfaz
                GlobalUi.getMainPage().getCalidadBensonUi().setText(Integer.toString((int) benson.getBonus()));
                GlobalUi.getMainPage().getCalidadTophUi().setText(Integer.toString((int) toph.getBonus()));
                GlobalUi.getMainPage().getBensonHP().setText(Integer.toString(benson.getCaballosFuerza()));
                GlobalUi.getMainPage().getTophHP().setText(Integer.toString(toph.getCaballosFuerza()));
                
                // setear en 0 el contador de inanicion de cada personaje
                if(benson != null){
                    benson.setContadorRondas(0);
                }
                if(toph != null){
                    toph.setContadorRondas(0);
                }
                
                updateColasUi();
                this.mutex.release();
                Thread.sleep(500);
//                this.mutex.acquire();
            }
            
        } catch (InterruptedException e){
            Logger.getLogger(Administrador.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    private void addPersonaje(String marca, int result) { // todo agregar Personaje a su cola de prioridad
//        int result = porcentaje.nextInt(100);
//        System.out.println("porcentaje para add Personaje: "+ result +"%");
        int prioridadFinal = Bonus();
        if (result <= 80) {
            if(marca.equals("Benson")){
//                    System.out.println("es benson y entro en el 80% por lo que se anade un personaje a la cola 1");
                Personaje benson = this.crearPersonaje(result, marca, prioridadFinal, prioridadFinal);
//                    System.out.println(benson);
                this.ponerPersonajeEnSuCola(benson);

            }else if(marca.equals("Toph")){
//                    System.out.println("es toph y se anade un personaje a la cola 1");
                Personaje toph = this.crearPersonaje(result, marca, prioridadFinal, prioridadFinal);
//                    System.out.println(toph);
                this.ponerPersonajeEnSuCola(toph);
            }
        }
    }
    
    private void createPersonajesIniciales(String marca){
        int prioridadFinal = Bonus();
        
        int result = porcentaje.nextInt(100);
        
        if(marca.equals("Benson")){
            
            Personaje benson = this.crearPersonaje(result, marca, prioridadFinal, prioridadFinal);
            
            
            if(benson.getPrioridad() == 1){
                this.bensonColaNivel1.encolar(benson);
            }else if(benson.getPrioridad() == 2){
                this.bensonColaNivel2.encolar(benson);
            }else if(benson.getPrioridad() == 3){
                this.bensonColaNivel3.encolar(benson);
            }                
        }else if(marca.equals("Toph")){ 
            Personaje toph = this.crearPersonaje(result, marca, prioridadFinal, prioridadFinal);
            if(toph.getPrioridad() == 1){
                this.tophColaNivel1.encolar(toph);
//                System.out.println(this.tophColaNivel1.print());
            }else if(toph.getPrioridad() == 2){
                this.tophColaNivel2.encolar(toph);
//                System.out.println(this.tophColaNivel2.print());
            }else if(toph.getPrioridad() == 3){
                this.tophColaNivel3.encolar(toph);
//                System.out.println(this.tophColaNivel3.print());
            }                
        }
    }
    
    private void desencolarRefuerzoPersonaje(Cola refuerzo) {
        if (refuerzo.isEmpty()) {
            return;
        }else {
            int result = porcentaje.nextInt(100);
            if (result <= 40) {
                Personaje Personaje = refuerzo.dispatch();
                Personaje.setPrioridad(1);
                this.regresarPersonajeCola1(Personaje);
            } else {
                Personaje Personaje = refuerzo.dispatch();
                refuerzo.encolar(Personaje);
            }
        }       
    }
    
    public void setCounter(int counter) {
        this.counter = counter;
    }
    
    public Personaje crearPersonaje(int id, String marca, int prioridad, int Bonus){
        return new Personaje(id, marca, prioridad, Bonus);
        
    }
    
    public void ponerPersonajeEnSuCola(Personaje personaje){
        if(personaje.getMarca().equals("Benson")){
            if(personaje.getPrioridad() == 1){
                this.bensonColaNivel1.encolar(personaje);
            }else if(personaje.getPrioridad() == 2){
                this.bensonColaNivel2.encolar(personaje);
            }else{
                this.bensonColaNivel3.encolar(personaje);
            }
        }else if(personaje.getMarca().equals("Toph")){
            if(personaje.getPrioridad() == 1){
                this.tophColaNivel1.encolar(personaje);
            }else if(personaje.getPrioridad() == 2){
                this.tophColaNivel2.encolar(personaje);
            }else{
                this.tophColaNivel3.encolar(personaje);
            }
        }
    }
    
    public void regresarPersonajeCola1(Personaje marca){
//        System.out.println("holaaaaa");
        if(marca.getMarca().equals("Benson")){
            this.bensonColaNivel1.encolar(marca);
        }else if(marca.getMarca().equals("Toph")){
//            System.out.println(marca + "entro a regresarPersonaje");
            this.tophColaNivel1.encolar(marca);
        }
    }
    
    private void sumarContadorCambiarPrioridad(Cola cola){
        int longitud = cola.getSize();
        int i = 0;
        
        while(i<longitud){
            Personaje Personaje = cola.dispatch();
            Personaje.setContadorRondas(Personaje.getContadorRondas() + 1);         
            
            if(Personaje.getContadorRondas()>=8){
                if (Personaje.getPrioridad()>1){
                    Personaje.setPrioridad(Personaje.getPrioridad() - 1);
                    if(Personaje.getMarca().equals("Benson")){
                        if(Personaje.getPrioridad() == 1){
                            this.bensonColaNivel1.encolar(Personaje);
                        }else if(Personaje.getPrioridad() == 2){
                            this.bensonColaNivel2.encolar(Personaje);
                        }else if(Personaje.getPrioridad() == 3){
                            this.bensonColaNivel3.encolar(Personaje);
                        }
                    }else if(Personaje.getMarca().equals("Toph")){
                        if(Personaje.getPrioridad() == 1){
                            this.tophColaNivel1.encolar(Personaje);
                        }else if(Personaje.getPrioridad() == 2){
                            this.tophColaNivel2.encolar(Personaje);
                        }else if(Personaje.getPrioridad() == 3){
                            this.tophColaNivel3.encolar(Personaje);
                        }
                    }
                }else{
                    cola.encolar(Personaje);
                }
                Personaje.setContadorRondas(1);
            }else{
                cola.encolar(Personaje);
            }
            i++;
        }
    }
    
    public void enviarpersonajesColaRefuerzo(Personaje lambito, Personaje bugga){
        if(lambito != null ){
            this.bensonRefuerzo.encolar(lambito);
        }
        if(bugga != null){
            this.tophRefuerzo.encolar(bugga);
        }
    }
    
    private Personaje getPersonajeColas(Cola cola1, Cola cola2, Cola cola3) {
        
        if (!cola1.isEmpty()) {
            return cola1.dispatch();
        } else if (!cola2.isEmpty()) {
            return cola2.dispatch();
        } else if (!cola3.isEmpty()) {
            return cola3.dispatch();
        }
        return null;
    }
    
    public void updateColasUi(){
        this.colaBensonUi1.updateUiQueue(bensonColaNivel1);
        this.colaBensonUi2.updateUiQueue(bensonColaNivel2);
        this.colaBensonUi3.updateUiQueue(bensonColaNivel3);
        this.colaBensonUiRef.updateUiQueue(bensonRefuerzo);
        
        this.colaTophUi1.updateUiQueue(tophColaNivel1);
        this.colaTophUi2.updateUiQueue(tophColaNivel2);
        this.colaTophUi3.updateUiQueue(tophColaNivel3);
        this.colaTophUiRef.updateUiQueue(tophRefuerzo);
    }
    
    public void regresarpersonajesAColas(Personaje benson, Personaje toph) {
        if (benson != null) {
            if(benson.getPrioridad() == 1){
                this.bensonColaNivel1.encolar(benson);
            }else if(benson.getPrioridad() == 2){
                this.bensonColaNivel2.encolar(benson);
            }else if(benson.getPrioridad() == 3){
                this.bensonColaNivel3.encolar(benson);
            }
        }
        if (toph != null) {
            if(toph.getPrioridad() == 1){
                this.tophColaNivel1.encolar(toph);
            }else if(toph.getPrioridad() == 2){
                this.tophColaNivel2.encolar(toph);
            }else if(toph.getPrioridad() == 3){
                this.tophColaNivel3.encolar(toph);
            }
        }
    }
    
    public int Bonus(){
        int PoderPersonajeAtaque = 0;
        int prioridadRandom = porcentaje.nextInt(100);
        if(prioridadRandom <= 60){
            PoderPersonajeAtaque = 1;
        }
        int PoderPersonajeDefensa = 0;
        prioridadRandom = porcentaje.nextInt(100);
        if (prioridadRandom <= 70){
            PoderPersonajeDefensa = 1;
        }
        
        int PoderPersonajeEspecialDMG = 0;
        prioridadRandom = porcentaje.nextInt(100);
        if (prioridadRandom <= 50){
            PoderPersonajeEspecialDMG = 1;
        }
        
        int calidadPersonaje = 0;
        prioridadRandom = porcentaje.nextInt(100);
        if (prioridadRandom <= 40){
            calidadPersonaje = 1;
        }
        int prioridadFinal = 0;
        int sumaCalidad = PoderPersonajeAtaque + calidadPersonaje + PoderPersonajeEspecialDMG + PoderPersonajeDefensa;
        if(sumaCalidad >= 3){
            prioridadFinal = 1;
        }else if(sumaCalidad == 2){
            prioridadFinal = 2;
        }else{
            prioridadFinal = 3;
        }
        return prioridadFinal;
    }
    
}
