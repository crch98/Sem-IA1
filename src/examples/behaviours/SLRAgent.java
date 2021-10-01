package examples.behaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class SLRAgent extends Agent {

	protected void setup() {
		System.out.println("Agent " + getLocalName() + " started.");
		addBehaviour(new SLRBehaviour());
	}

	private class LinearRegression {

		private double beta_0;
		private double beta_1;

		public LinearRegression() {
			beta_0 = 0.0;
			beta_1 = 0.0;
		}

		public double predict(double x) {
			return (beta_1 * x) + beta_0;
		}

		private void gradient_descent(double[][] dataset, double alpha, int epoch) {
			int X_POSITION = 0;
			int Y_POSITION = 1;

			for (int i = 0; i < epoch; i++) {
				for (int j = 0; j < dataset.length; j++) {
					var y_p = predict(dataset[j][X_POSITION]);
					var error = y_p - dataset[j][Y_POSITION];
					beta_0 = beta_0 - (error * alpha);
					beta_1 = beta_1 - (error * alpha * dataset[j][X_POSITION]);
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
			double learning_rate = 0.0001;
			int num_epoch = 30000;

			ln.train(dataset, learning_rate, num_epoch);

			System.out.println("Regression equation:");
			System.out.println("y_p = " + ln.getBeta_0() + " + " +
				ln.getBeta_1() + " * X");

			System.out.println("Predicted value of " + 80 + ": " +
				ln.predict(80));
		}

		public int onEnd() {
			myAgent.doDelete();
			return super.onEnd();
		}

	}

}




