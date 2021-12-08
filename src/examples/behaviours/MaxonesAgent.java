package examples.behaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MaxonesAgent extends Agent {

    protected void setup() {
        System.out.println("Agent " + getLocalName() + " started.");

        addBehaviour(new GABehaviour());
    }

    private class GA {

        private int populationSize;
        private int individualSize;
        private int numberOfGenerations;

        private class Chromosome {
            String genes;
            int fitness;
            double probability;

            public Chromosome() {
                genes = "";
                fitness = 0;
                probability = 0.0;
            }

            public Chromosome(String g) {
                genes = g;
                fitness = 0;
                probability = 0.0;
            }

            public String getGenes() {
                return genes;
            }

            public void setGenes(String genes) {
                this.genes = genes;
            }

            public int getFitness() {
                return fitness;
            }

            public void setFitness(int fitness) {
                this.fitness = fitness;
            }

            public double getProbability() {
                return probability;
            }

            public void setProbability(double probability) {
                this.probability = probability;
            }

            public String toString() {
                return "Genes: " + genes + " Fitness: " + fitness + " Probability: " +
                    probability;
            }
        }

        public GA(int pSize, int iSize, int nGens) {
            populationSize = pSize;
            individualSize = iSize;
            numberOfGenerations = nGens;
        }

        public ArrayList<Chromosome> generatePopulation() {
            ArrayList<Chromosome> population = new ArrayList<>();
            final String alphabet = "01";
            StringBuilder sb = new StringBuilder();
            Random random = new Random();

            for (int i = 0; i < populationSize; i++) {
                // creates random individual
                for (int j = 0; j < individualSize; j++) {
                    int index = random.nextInt(alphabet.length());
                    sb.append(alphabet.charAt(index));
                }

                population.add(new Chromosome(sb.toString()));
                sb.setLength(0);
            }

            return population;
        }

        public int individualFitness(String ind) {
            int count = 0;

            for (int i = 0; i < ind.length(); i++) {
                if (ind.charAt(i) == '1') {
                    count++;
                }
            }

            return count;
        }

        public int populationFitness(ArrayList<Chromosome> population) {
            int totalFitness = 0;
            int indFitness = 0;

            for (var individual : population) {
                indFitness = individualFitness(individual.getGenes());
                individual.setFitness(indFitness);

                totalFitness += indFitness;
            }

            return totalFitness;
        }

        public void setPopulationProbability(ArrayList<Chromosome> population) {
            int totalFitness = populationFitness(population);

            for (var individual : population) {
                double indProbability = (double) individual.getFitness() /
                    (double) totalFitness;

                individual.setProbability(indProbability);
            }
        }

        public int[] rouletteSelection(ArrayList<Chromosome> population) {
            setPopulationProbability(population);

            double total = 0.0;
            HashMap<Integer, ArrayList<Double>> slices = new HashMap<>();

            for (int i = 0; i < populationSize; i++) {
                ArrayList<Double> values = new ArrayList<>();
                values.add(total);
                var indProbability = population.get(i).getProbability();
                values.add(total + indProbability);
                slices.put(i, values);

                total += indProbability;
            }

            int[] result = new int[populationSize];

            for (int i = 0; i < populationSize; i++) {
                double spin = Math.random();

                for (var key: slices.keySet()) {
                    var slice = slices.get(key);

                    if (slice.get(0) < spin && spin <= slice.get(1)) {
                        result[i] = key;
                        break;
                    }
                }
            }

            return result;
        }

        public ArrayList<Chromosome>
        onePointCrossover(Chromosome parentA, Chromosome parentB) {
            var parentAGenes = parentA.getGenes();
            var parentBGenes = parentB.getGenes();
            ArrayList<Chromosome> children = new ArrayList<>();
            Random random = new Random();
            int xoverPoint = random.nextInt(parentAGenes.length());

            var firstChild = parentAGenes.substring(0, xoverPoint) + 
                parentBGenes.substring(xoverPoint, parentAGenes.length());

            var secondChild = parentBGenes.substring(0, xoverPoint) + 
                parentAGenes.substring(xoverPoint, parentAGenes.length());

            children.add(new Chromosome(firstChild));
            children.add(new Chromosome(secondChild));

            return children;
        }

        public void mutateIndividual(ArrayList<Chromosome> children, int mutationRate) {
            Random random = new Random();

            for (var child : children) {
                int idx = random.nextInt(mutationRate);
                StringBuilder sb = new StringBuilder(child.getGenes());

                if (sb.charAt(idx) == '1') {
                    sb.setCharAt(idx, '0');
                } else {
                    sb.setCharAt(idx, '1');
                }

                child.setGenes(sb.toString());
            }
        }

        public ArrayList<Chromosome> reproduceChildren(int[] chosen, ArrayList<Chromosome> population) {
            ArrayList<Chromosome> children = new ArrayList<>();

            for (int i = 0; i < (chosen.length / 2 - 1); i++) {
                children.addAll(onePointCrossover(population.get(chosen[i]),
                    population.get(chosen[i + 1])));
            }

            return children;
        }

        public Chromosome getBest(ArrayList<Chromosome> population) {
            int bestFitness = 0;
            Chromosome bestIndividual = new Chromosome();

            for (var individual : population) {
                if (individual.getFitness() > bestFitness) {
                    bestFitness = individual.getFitness();
                    bestIndividual = individual;
                }
            }

            return bestIndividual;
        } 

        public void runGA() {
            int bestGlobalFitness = 0;
            var globalPopulation = generatePopulation();

            for (int i = 0; i < numberOfGenerations; i++) {
                int currentBestFitness = populationFitness(globalPopulation);

                if (currentBestFitness > bestGlobalFitness) {
                    bestGlobalFitness = currentBestFitness;
                }

                var chosen = rouletteSelection(globalPopulation);
                var children = reproduceChildren(chosen, globalPopulation);
                mutateIndividual(children, individualSize);
                globalPopulation.addAll(children);
            }

            var best = getBest(globalPopulation);

            System.out.println("Best fitness: " + bestGlobalFitness);
            System.out.println("Actual best: " + best.getGenes());
            System.out.println("Actual best fitness: " + best.getFitness());
        }

    }

    private class GABehaviour extends OneShotBehaviour {

        public void action() {
            GA g = new GA(100, 10, 1000);

            g.runGA();
        }

        public int onEnd() {
			myAgent.doDelete();
			return super.onEnd();
		}
        
    }

}