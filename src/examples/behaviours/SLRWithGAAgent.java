package examples.behaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SLRWithGAAgent extends Agent {

    protected void setup() {
        System.out.println("Agent " + getLocalName() + " started.");

        addBehaviour(new GABehaviour());
    }

    private class GA {

        private int populationSize;
        private int individualSize;
        private int numberOfGenerations;
        ArrayList<Chromosome> population;
        private final int min = 0;
        private final int max = 300;
        private final double[][] dataset = {
            {23, 651},  {26, 762},  {30, 856},
            {34, 1063}, {43, 1190}, {48, 1298}, 
            {52, 1421}, {57, 1440}, {58, 1518}
	    };

        private class Chromosome {
            double[] genes;
            int fitness;
            double probability;

            public Chromosome() {
                genes = new double[2];
                fitness = 0;
                probability = 0.0;
            }

            public Chromosome(double[] g) {
                genes = g;
                fitness = 0;
			    probability = 0.0;
		    }

            public double[] getGenes() {
                return genes;
            }

            public void setGenes(double[] genes) {
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
                StringBuilder bs = new StringBuilder("[");

                for (int i = 0; i < genes.length; i++) {
                    if (i == (genes.length - 1)) {
                        bs.append(genes[i]);
                    } else {
                        bs.append(genes[i] + " ");
                    }
                }

                bs.append("]");

                return "Genes: " + bs.toString();
            }
        }

        public GA(int pSize, int iSize, int nGens) {
            populationSize = pSize;
            individualSize = iSize;
            numberOfGenerations = nGens;
            population = new ArrayList<Chromosome>();
        }

        public void generatePopulation() {
            for (int i = 0; i < populationSize; i++) {
                // creates random individual
                double[] genes = new double[individualSize];

                for (int j = 0; j < individualSize; j++) {
                    double number = Math.random() * (max - min + 1) + min;
                    genes[j] = number;
                }

                population.add(new Chromosome(genes));
            }
        }

        public int individualFitness(double[] ind) {
            double error = Math.abs(squaredError(ind));
            double fitness = 0;

            error = error / 100.0;
            fitness = Math.abs(100.0 - error);
    
            return (int) fitness;
        }

        private double predict(double x, double[] ind) {
            return ind[0] + (x * ind[1]);
        }

        private double squaredError(double[] ind) {
            double totalError = 0.0;

            for (int i = 0; i < dataset.length; i++) {
                double error = dataset[i][1] - predict(dataset[i][0], ind); //revisar

                totalError += error;
            }

            return totalError / (double) dataset.length;
        }

        public int populationFitness() {
            int totalFitness = 0;
            int indFitness = 0;

            for (var individual : population) {
                indFitness = individualFitness(individual.getGenes());
                individual.setFitness(indFitness);

                totalFitness += indFitness;
            }

            return totalFitness;
        }

        public void setPopulationProbability() {
            int totalFitness = populationFitness();

            for (var individual : population) {
                double indProbability = (double) individual.getFitness() /
                    (double) totalFitness;

                individual.setProbability(indProbability);
            }
        }

        public int[] rouletteSelection() {
            setPopulationProbability();

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

        public ArrayList<Chromosome> onePointCrossover(Chromosome parentA, Chromosome parentB) {
            var parentAGenes = parentA.getGenes();
            var parentBGenes = parentB.getGenes();
            ArrayList<Chromosome> children = new ArrayList<>();
            Random random = new Random();
            int xoverPoint = random.nextInt(individualSize);
            double[] firstChild = new double[individualSize];
            double[] secondChild = new double[individualSize];

            for (int i = 0; i < xoverPoint; i++) {
                firstChild[i] = parentAGenes[i];
            }

            for (int i = xoverPoint; i < individualSize; i++) {
                firstChild[i] = parentBGenes[i];
            }

            for (int i = 0; i < xoverPoint; i++) {
                secondChild[i] = parentBGenes[i];
            }

            for (int i = xoverPoint; i < individualSize; i++) {
                secondChild[i] = parentAGenes[i];
            }

            children.add(new Chromosome(firstChild));
            children.add(new Chromosome(secondChild));

            return children;
        }

        public void mutateIndividual(ArrayList<Chromosome> children, int mutationRate) {
            Random random = new Random();
            int idx = 0;
            double newValue = 0.0;

            for (var child : children) {
                idx = random.nextInt(mutationRate);
                newValue = Math.random() * (max - min + 1) + min;
                var genes = child.getGenes();
                genes[idx] = newValue;
                child.setGenes(genes);
            }
        }

        public ArrayList<Chromosome> reproduceChildren(int[] chosen) {
            ArrayList<Chromosome> children = new ArrayList<>();

            for (int i = 0; i < (chosen.length / 2 - 1); i++) {
                children.addAll(onePointCrossover(population.get(chosen[i]),
                    population.get(chosen[i + 1])));
            }

            return children;
        }

        public Chromosome getBest() {
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
            generatePopulation();

            for (int i = 0; i < numberOfGenerations; i++) {
                int currentBestFitness = populationFitness();

                if (currentBestFitness > bestGlobalFitness) {
                    bestGlobalFitness = currentBestFitness;
                }

                var chosen = rouletteSelection();
                var children = reproduceChildren(chosen);
                mutateIndividual(children, individualSize);
                population.addAll(children);
            }

            var best = getBest();

            System.out.println("Best fitness: " + bestGlobalFitness);
            System.out.println("Actual best: " + best.toString());
            System.out.println("Actual best fitness: " + best.getFitness());
            System.out.println("------------------------------------------------");
        }

    }

    private class GABehaviour extends OneShotBehaviour {

        public void action() {
            GA g = new GA(100, 2, 1000);

            g.runGA();
        }

        public int onEnd() {
			myAgent.doDelete();
			return super.onEnd();
		}

    }
    
}