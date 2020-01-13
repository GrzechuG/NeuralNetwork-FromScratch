/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geneticalgorithm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Grzes
 */
public class GeneticAlgorithm {

    /**
     * @param args the command line arguments
     */
    public ArrayList<double[]> inputs = new ArrayList();
    public ArrayList<double[]> outputs = new ArrayList();
    public ArrayList<double[]> weights = new ArrayList();
    Calc cl = new Calc();
    int inputs_num;
    int outputs_num;
    

    int[] hidden_layers_nums = new int[]{6};
    int weights_num;
    int generation = 0;

    double[] bestWeights;
    double bestScore = 90000;
    int population_size = 1000;
    // public ArrayList<double[]> globalPop = new ArrayList<>();

    String save_folder = "D:\\Stuff\\Programs\\train_gen\\";
    boolean finished=false;
    public GeneticAlgorithm(String[] args) {
        //Setting up

        //TODO: Loading csv!
//        args = new String[1];
//        args[0] = "--simulate";
        if (args.length > 0) {
            if (args[0].contains("--simulate")) {
                optionSimulate();

            }

            save_folder = args[0];
            Scanner sc = new Scanner(System.in);
            System.out.println("Location of .conf file of new neural net:");
            String loc = sc.nextLine();
            String content = loadFile(loc);
            String[] lines = content.split("\n");
            String[] test = lines[0].split(": ");
            inputs_num = Integer.parseInt(test[1].replace(" ",""));
            outputs_num = Integer.parseInt(lines[1].split(": ")[1].replace(" ",""));
           if(!lines[2].split(": ")[1].equals("[]")){
            String values[] = (lines[2].split(": ")[1]).replace("[", "").replace("]", "").replace(" ","").split(",");
            
            int[] parsed = new int[values.length];
            for (int i = 0; i < values.length; i++) {
                parsed[i] = Integer.parseInt(values[i]);
            }
            hidden_layers_nums = parsed;
           }else{
                hidden_layers_nums = new int[]{};
           }
            population_size= Integer.parseInt(lines[3].split(": ")[1]);

        } else {
            System.out.println("Usage: java -jar GeneticAlgorithm.jar save_folder ");
            System.out.println("--simulate for simulating neural net.");
            save_folder = "./";
        }
        loadData(save_folder + "inputs.csv", save_folder + "outputs.csv");

        outputs_num = outputs.get(0).length;
        inputs_num = inputs.get(0).length;
        int previous = inputs_num;
        for (int i = 0; i < hidden_layers_nums.length; i++) {
            int hidden_layers_num = hidden_layers_nums[i];
            weights_num += hidden_layers_num * previous;
            previous = hidden_layers_num;
        }
        weights_num += previous * outputs_num;
        //weigths_num = hidden1_num*inputs_num+hidden1_num*hidden2_num+outputs_num*hidden2_num;
        bestWeights = new double[weights_num];
        System.out.println("Inputs:" + inputs_num);
        System.out.println("Outputs:" + outputs_num);
        System.out.println("Weights:" + weights_num);
        System.out.println("Hidden layer: " + Arrays.toString(hidden_layers_nums));
        System.out.println("Population size:" + population_size);
        //Generating population
        ArrayList<double[]> population = generatePopulation(population_size, 0.0, 1.0);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                finished=true;
                String csvFormat = "";

                System.out.println(Arrays.toString(bestWeights));
                csvFormat += Arrays.toString(bestWeights).replace("[", "").replace("]", "");
                //TODO: generate CSV with weights
                try {
                    FileWriter fw = new FileWriter(save_folder + "weights.csv");
                    fw.write(csvFormat);
                    fw.close();
                } catch (Exception e) {
                    System.out.println(e);
                }

                try {
                    FileWriter ff = new FileWriter(save_folder + "net.nfo");
                    ff.write("Inputs: " + inputs_num + "\n"
                            + "Outputs: " + outputs_num
                            + "\nWeights: " + weights_num
                            + "\nHidden layers: " + Arrays.toString(hidden_layers_nums)
                            + "\nWeightsFile:" + save_folder
                            + "weights.csv");
                    ff.close();
                } catch (Exception e) {
                    System.out.println(e);
                }
                optionSimulate();

                //Todo choose to test
            }

            /*
       my shutdown code here
             */
        }
        );
        while (true) {
            if(!finished)
            population = Gen(population);

        }
    }

    public void optionSimulate() {
        Scanner sc = new Scanner(System.in);

        // String input 
        System.out.println("Location of .nfo file, q - exit:");
        String loc = sc.nextLine();
        if(loc.equals("q")){
            System.out.println("Goodbye..");
            Runtime.getRuntime().halt(0);
            System.exit(0);
        }
        String content = loadFile(loc);
        String[] lines = content.split("\n");
        String[] test = lines[0].split(": ");
        inputs_num = Integer.parseInt(test[1].replace(" ",""));
        outputs_num = Integer.parseInt(lines[1].split(": ")[1].replace(" ",""));
        weights_num = Integer.parseInt(lines[2].split(": ")[1].replace(" ",""));
        String values[] = (lines[3].split(": ")[1]).replace("[", "").replace("]", "").replace(" ","").split(",");
        int[] parsed = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            parsed[i] = Integer.parseInt(values[i]);
        }
        hidden_layers_nums = parsed;

        String weights_file = lines[4].split(":")[1];

        double[] weights = loadWeights(weights_file);
        while (true) {
            simulate(inputs_num, outputs_num, weights_num, hidden_layers_nums, weights);
        }
    }

    public void simulate(int inputs_num, int outputs_num, int weights_num, int[] hidden_layers, double[] weights) {
        Scanner sc = new Scanner(System.in);

        // String input 
        System.out.println("Input to test: (comma separated )");
        String inp[] = sc.nextLine().split(",");
        double[] gen = new double[inp.length];
        for (int i = 0; i < inp.length; i++) {
            double num = Double.parseDouble(inp[i]);
            gen[i] = num;
        }
        System.out.println(Arrays.toString(evaluateNeuralNet(gen, weights, outputs_num, 0, 0)));
    }

    public ArrayList<double[]> generatePopulation(int size, double rangeMin, double rangeMax) {
        ArrayList popuplation = new ArrayList();
        for (int i = 0; i < size; i++) {
            double[] temp = new double[weights_num];
            for (int j = 0; j < weights_num; j++) {
                Random r = new Random();
                double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
                temp[j] = randomValue;

            }
            popuplation.add(temp);

        }

        return popuplation;
    }

    public ArrayList<double[]> MutatePop(ArrayList<double[]> population, double rangeMin, double rangeMax) {
        //change
        ArrayList<double[]> mutations = new ArrayList();
        for (int i = 0; i < population.size(); i++) {
            double[] get = population.get(i);
            for (int h = 0; h < 2; h++) {
                //2 - oznacza stagnacje w ilosci populacji; 2> - malejaca populacja; 2< - rosnaca populacja

                for (int j = 0; j < get.length; j++) {
                    Random r = new Random();
                    double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
                    get[j] += randomValue;
                }
                mutations.add(get);
            }

        }
        return mutations;
    }

    public ArrayList<double[]> Crossover(ArrayList<double[]> population) {
        ArrayList<double[]> pop2 = new ArrayList();

        int size = population.size();
        for (int i = 0; i < population.size(); i++) {

            int rangeMin = 0;
            int rangeMax = population.size() - 1;

            Random r = new Random();
            int randomDaddyInd = r.nextInt((rangeMax - rangeMin) + 1) + rangeMin;
            int randomMommyInd = (new Random()).nextInt((rangeMax - rangeMin) + 1) + rangeMin;
            while (true) {
                if (randomMommyInd != randomDaddyInd) {
                    break;
                }
                randomMommyInd = (new Random()).nextInt((rangeMax - rangeMin) + 1) + rangeMin;
            }

            double newBaby[] = new double[weights_num];
            for (int j = 0; j < newBaby.length; j++) {
                if (j <= weights_num) {
                    newBaby[j] = population.get(randomMommyInd)[j];
                } else {
                    newBaby[j] = population.get(randomDaddyInd)[j];
                }

            }
            pop2.add(newBaby);
            population.remove(randomMommyInd);

        }
        return pop2;
    }

    double mutation_step = 0.5;

    public double[] evaluateNeuralNet(double[] input, double[] weights, int outputs_number, int weight_index_start, int layer) {
        int original_outputs_num = outputs_number;

        int weight_index = weight_index_start;
        if (layer != hidden_layers_nums.length) {
            outputs_number = hidden_layers_nums[layer];
        }

        double[] output = new double[outputs_number];
        for (int j = 0; j < outputs_number; j++) {
            for (int i = 0; i < input.length; i++) {

                double inp = input[i];
                output[j] += (inp * weights[weight_index]);
                weight_index++;

            }
            output[j] = cl.sigmoid(output[j]);

        }
        if (layer != hidden_layers_nums.length) {
            output = evaluateNeuralNet(output, weights, original_outputs_num, weight_index, layer + 1);
            return output;
            //Unchanged
        }
        return output;
    }

    public ArrayList<Double> FitnessFunction(ArrayList<double[]> population) {
        ArrayList<Double> errors = new ArrayList();
        for (int i = 0; i < population.size(); i++) {
            double[] pop = population.get(i);
            double error = 0;
            for (int j = 0; j < inputs.size(); j++) {
                double[] inpSet = inputs.get(j);
                double[] outSet = outputs.get(j);

                double[] output = evaluateNeuralNet(inpSet, pop, outputs_num, 0, 0);
                for (int k = 0; k < output.length; k++) {
                    error += Math.pow(output[k] - outSet[k], 2);

                }
            }
            errors.add((double) Math.sqrt(error));
        }

        return errors;
    }

    public ArrayList<Double> diversityScoring(ArrayList<double[]> population) {
        ArrayList<Double> diffs = new ArrayList();
        for (int i = 0; i < population.size(); i++) {
            double diff = 0;
            double[] get = population.get(i);
            for (int j = 0; j < population.size(); j++) {
                double[] get1 = population.get(j);
                for (int k = 0; k < get.length; k++) {
                    diff += Math.pow(get[k] - get1[k], 2);

                }
            }
            diffs.add((double) Math.sqrt(diff));

        }
        return diffs;
    }

    public ArrayList<double[]> Gen(ArrayList<double[]> population) {
        // globalPop=population;
        generation++;

        //Mutation
        ArrayList<double[]> newPopulation = new ArrayList<>();
        newPopulation = MutatePop(population, -mutation_step, mutation_step);
        //System.out.println(newPopulation.size());

        //Crossover
        newPopulation = Crossover(newPopulation);
        //System.out.println(newPopulation.size());

        //Fitness
        ArrayList<Double> fitness = FitnessFunction(newPopulation);

        //Diversity
        ArrayList<Double> diversity = diversityScoring(newPopulation);

        //Best desired point:
        double diversityX = 1;
        double fitnessY = 0;

        ArrayList<Double> distances = new ArrayList<>();
//        //Propability #3 + diversity?
        if (generation < 0) {
            for (int i = 0; i < newPopulation.size(); i++) {
                double[] get = newPopulation.get(i);
                double fit = fitness.get(i);
                double div = diversity.get(i);
                double distance = Math.sqrt(Math.pow(fit - fitnessY, 2) + Math.pow(div - diversityX, 2));
                distances.add(distance);
                //System.out.println(get + " Fit:" + fitness.get(i) + " Diversity:" + div + " Dst: " + distance);
                //Score fitness + propablity method #3
            }
        } else {
            distances = fitness;
        }
        ArrayList<double[]> top100 = new ArrayList();
        for (int j = 0; j < population_size / 2; j++) {
            double[] best = new double[weights_num];
            int bestID = 0;
            double lowest = 10000;
            for (int i = 0; i < distances.size(); i++) {
                double dst = distances.get(i);
                if (lowest >= dst) {
                    lowest = dst;

                    System.arraycopy(newPopulation.get(i), 0, best, 0, best.length);
                    //  best=newPopulation.get(i);
                    bestID = i;

                    //Actually remove rest, but it is unnecessary.
                }

            }
            if (j == 0) {
                // System.out.println("Best Fit:" + distances.get(bestID)+" "+Arrays.toString(best));
                if (bestScore > distances.get(bestID)) {
                    bestScore = distances.get(bestID);
                    System.out.println("Best so far:" + " Score:" + bestScore + " Generation:" + generation);
                    System.arraycopy(best, 0, bestWeights, 0, bestWeights.length);
                }
            }
            //System.out.println(distances.get(bestID));

            top100.add(best);
            distances.remove(bestID);
            newPopulation.remove(bestID);
        }
        for (int j = 0; j < population_size / 2; j++) {
            double[] best = new double[weights_num];
            int bestID = 0;
            double lowest = 10000;
            for (int i = 0; i < distances.size(); i++) {
                double dst = distances.get(i);
                if (lowest >= dst) {
                    lowest = dst;

                    System.arraycopy(population.get(i), 0, best, 0, best.length);
                    //  best=population.get(i);
                    bestID = i;

                    //Actually remove rest, but it is unnecessary.
                }

            }

            //System.out.println(fitness.get(bestID));
            top100.add(best);
            distances.remove(bestID);
            population.remove(bestID);
        }

        return top100;
    }

    public static void main(String[] args) {
        System.out.println("GeneticAlgorithm program just started.");
        new GeneticAlgorithm(args);

    }

    public String loadFile(String file) {
        String contents = "";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                contents += line + "\n";
                System.out.println(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(GeneticAlgorithm.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return contents;
    }

    public void loadData(String input_file, String output_file) {
        System.out.println("Loading training data...");
        String file = input_file;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {

                String[] genS = line.split(",");
                double[] gen = new double[genS.length];
                for (int i = 0; i < genS.length; i++) {
                    double num = Double.parseDouble(genS[i]);
                    gen[i] = num;
                }
                inputs.add(gen);

            }
        } catch (IOException ex) {
            Logger.getLogger(GeneticAlgorithm.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        file = output_file;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {

                String[] genS = line.split(",");
                double[] gen = new double[genS.length];
                for (int i = 0; i < genS.length; i++) {
                    double num = Double.parseDouble(genS[i]);
                    gen[i] = num;
                }
                outputs.add(gen);

            }
        } catch (IOException ex) {
            Logger.getLogger(GeneticAlgorithm.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
//        inputs.add(new double[]{1, 0, 1, 1, 0});
//        outputs.add(new double[]{0, 1, 1, 0, 1});
//        inputs.add(new double[]{0, 0, 1, 0, 1});
//        outputs.add(new double[]{1, 0, 1, 0, 0});
//        inputs.add(new double[]{1, 0, 0, 0, 0});
//        outputs.add(new double[]{0, 0, 0, 0, 1});
//        inputs.add(new double[]{1, 1, 0, 0, 1});
//        outputs.add(new double[]{1, 0, 0, 1, 1});
//        inputs.add(new double[]{0, 1, 1, 0, 1});
//        outputs.add(new double[]{1, 0, 1, 1, 0});
//        inputs.add(new double[]{0, 0, 0, 1, 1});
//        outputs.add(new double[]{1, 1, 0, 0, 0});
//        inputs.add(new double[]{1, 1, 0, 1, 0});
//        outputs.add(new double[]{0, 1, 0, 1, 1});

    }

    private double[] loadWeights(String weights_file) {
        double[] out;
        try (BufferedReader br = new BufferedReader(new FileReader(weights_file))) {
            String line;
            while ((line = br.readLine()) != null) {

                String[] genS = line.split(",");
                double[] gen = new double[genS.length];
                for (int i = 0; i < genS.length; i++) {
                    double num = Double.parseDouble(genS[i]);
                    gen[i] = num;
                }
                return gen;

            }
        } catch (IOException ex) {
            Logger.getLogger(GeneticAlgorithm.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
