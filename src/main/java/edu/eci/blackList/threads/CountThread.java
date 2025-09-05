/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.threads;

/**
 *
 * @author hcadavid
 */
public class CountThread extends Thread{

    private int A, B;

    CountThread(int a, int b){
        this.A = a;
        this.B = b;
    }

    @Override
    public void run() {
        System.out.println("\nCountThread is running...");
        
        for (int i = A ; i <= B; i++){
            if (i % 10 == 0){
                System.out.println("");
            }
            System.out.print(i + " | ");
        }

        System.out.println("\nCountThread finished...");
    }
    
}
