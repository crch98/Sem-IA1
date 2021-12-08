package examples.behaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GAEquationAgent extends Agent {

    protected void setup() {
        System.out.println("Agent " + getLocalName() + " started.");

        addBehaviour(new GABehaviour());
    }

    private class GA {

        private int populationSize;
        private int individualSize;
        private int numberOfGenerations;
        ArrayList<Chromosome> population;

        private class Chromosome {
            int[] genes;
            int fitness;
            double probability;

            public Chromosome() {
                genes = new int[5];
                fitness = 0;
                probability = 0.0;
            }

            public Chromosome(int[] g) {
                genes = g;
                fitness = 0;
                probability = 0.0;
            }

            public int[] getGenes() {
                return genes;
            }

            public void setGenes(int[] genes) {
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
                    bs.append(genes[i] + " ");
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
            final int maxNumber = 10;
            Random random = new Random();

            for (int i = 0; i < populationSize; i++) {
                // creates random individual
                int[] genes = new int[5];

                for (int j = 0; j < individualSize; j++) {
                    int number = random.nextInt(maxNumber);
                    genes[j] = number;
                }

                population.add(new Chromosome(genes));
            }
        }

        public int individualFitness(int[] ind) {
            // evaluate a + 2b - 3c + d + 4e = 30
            int result = ind[0] + (2 * ind[1]) - (3 * ind[2]) +
                ind[3] + (4 * ind[4]);
            int fitness = 0;

            if (result == 30) {
                fitness = 30;
            } else {
                fitness = result % 31;
            }
    
            return fitness;
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

        public ArrayList<Chromosome>
        onePointCrossover(Chromosome parentA, Chromosome parentB) {
            var parentAGenes = parentA.getGenes();
            var parentBGenes = parentB.getGenes();
            ArrayList<Chromosome> children = new ArrayList<>();
            Random random = new Random();
            int xoverPoint = random.nextInt(individualSize);
            int[] firstChild = new int[individualSize];
            int[] secondChild = new int[individualSize];

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
            int newValue = 0;

            for (var child : children) {
                idx = random.nextInt(mutationRate);
                newValue = random.nextInt(10);
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
            GA g = new GA(100, 5, 100);

            g.runGA();
        }

        public int onEnd() {
			myAgent.doDelete();
			return super.onEnd();
		}

    }

}