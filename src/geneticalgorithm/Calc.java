package geneticalgorithm;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Grzes
 */
public class Calc {
    public Calc(){
        
    }
    public double sigmoid(double x) {
    return (1/( 1 + Math.pow(Math.E,(-1*x))));
  }
}
