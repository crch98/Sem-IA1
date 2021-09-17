package examples.behaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class GradientDescentAgent extends Agent {

	protected void setup() {
		System.out.println("Agent " + getLocalName() + " started.");
		addBehaviour(new GDOneShotBehaviour());
	}

	/**
	 * Implements Gradient Descent algorithm for function f(x) = x^2
	 * with bounds [-1, 1].
	 *
     */
	private class GradientDescent {
		private double stepSize;
		private int nIter;

		public GradientDescent(double stepSize, int nIter) {
			this.stepSize = stepSize;
			this.nIter = nIter;
		}

		/**
		 * double -> double
		 *
		 * produces the square of parameter x
		 * this is f(x) = x^2
		 *
		 */
		private double objective(double x) {
			return x * x;
		}

		/**
  		 * double -> double
		 *
		 * produces the result of multiplying x by 2
		 * this is the derivative of f(x)
		 * f'(x) = 2x
		 *
		 */
		private double derivative(double x) {
			return 2 * x;
		}

		/**
		 * shows the result obtained from each iteration of the
		 * gradient descent algorithm
		 *
		 */
		public void calculate() {
			double leftBound = -1.0;
			double rightBound = 1.0;

			// initialize to a random number in the range [-1, 1]
			double solution = leftBound + (Math.random() *
				(rightBound - leftBound));

			System.out.println("------------------------------");
			System.out.printf("%-8s | %-8s | %s\n", "n_iter", "x value", "f(x)");
			System.out.println("------------------------------");
			for (int i = 0; i < this.nIter; i++) {
				var gradient = derivative(solution);
				// update solution value
				solution = solution - (gradient * this.stepSize);
				var result = objective(solution);
				// show the new solution
				System.out.printf("%-8d | %-8.5f | %.5f\n", i, solution, result);
			}
		}

	}

	private class GDOneShotBehaviour extends OneShotBehaviour {

		public void action() {
			GradientDescent gd = new GradientDescent(0.1, 30);

			gd.calculate();
		}

		public int onEnd() {
			myAgent.doDelete();
			return super.onEnd();
		}

	}

}
