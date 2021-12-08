package examples.behaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class SLRAgent extends Agent {

	private double predictValue;

	protected void setup() {
		System.out.println("Agent " + getLocalName() + " started.");

		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			var aux = (String) args[0];
			try {
				predictValue = Double.parseDouble(aux);
			} catch (NumberFormatException e) {
				System.err.println("Error: " + e);
				doDelete();
				System.exit(0);
			}
		} else {
			doDelete();
			System.exit(0);
		}

		addBehaviour(new SLRBehaviour());
	}

	private class LinearRegression {

		private double beta_0;
		private double beta_1;

		private static final int X_POSITION = 0;
		private static final int Y_POSITION = 1;

		public LinearRegression() {
			beta_0 = 0.0;
			beta_1 = 0.0;
		}

		public double predict(double x) {
			return (beta_1 * x) + beta_0;
		}

		private double error(double y_i, double y_p) {
			return y_i - y_p;
		}

		private double partialDerivB1(double x, double y, int n) {
			var e = error(y, predict(x));

			return ((double) (-2) / n) * x * e;
		}

		private double partialDerivB0(double x, double y, int n) {
			var e = error(y, predict(x));
	
			return ((double) (-2) / n) * e;
		} 

		private void gradient_descent(double[][] dataset, double alpha, int epoch) {
			int N = dataset.length;

			for (int i = 0; i < epoch; i++) {
				for (int j = 0; j < dataset.length; j++) {
					var x_i = dataset[j][X_POSITION];
					var y_i = dataset[j][Y_POSITION];

					beta_0 -= (alpha * partialDerivB0(x_i, y_i, N));
					beta_1 -= (alpha * partialDerivB1(x_i, y_i, N));
				}
			}
		}

		public double getBeta_0() {
			return beta_0;
		}

		public double getBeta_1() {
			return beta_1;
		}

		public void train(double[][] dataset, double alpha, int epoch) {
			gradient_descent(dataset, alpha, epoch);
		}

	}

	private class SLRBehaviour extends OneShotBehaviour {

		private final double [][] dataset = {
			{23, 651},  {26, 762},  {30, 856},
			{34, 1063}, {43, 1190}, {48, 1298}, 
			{52, 1421}, {57, 1440}, {58, 1518}
    	};

		public void action() {
			LinearRegression ln = new LinearRegression();
			double learning_rate = 0.001;
			int num_epoch = 13100;

			ln.train(dataset, learning_rate, num_epoch);

			System.out.println("Regression equation:");
			System.out.println("y_p = " + ln.getBeta_0() + " + " +
				ln.getBeta_1() + " * X");

			System.out.println("Predicted value of " + predictValue + ": " +
				ln.predict(predictValue));
		}

		public int onEnd() {
			myAgent.doDelete();
			return super.onEnd();
		}

	}

}




