package hmw;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class GradientDescentAgent extends Agent {

	protected void setup() {
		System.out.println("Agent " + getLocalName() + " started.");
		addBehaviour(new GDOneShotBehaviour());
	}

	private class GDOneShotBehaviour extends OneShotBehaviour {

		public void action() {}

		public int onEnd() {
			myAgent.doDelete();
			return super.onEnd();
		}

	}

}
