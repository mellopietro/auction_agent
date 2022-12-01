package template;

//the list of imports
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import logist.behavior.AuctionBehavior;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;
import solution.AgentVehicle;
import solution.Solution;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 * 
 */
@SuppressWarnings("unused")
public class AuctionAgent implements AuctionBehavior {

	private Solution solution;
	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private Vehicle vehicle;
	private City currentCity;

	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.vehicle = agent.vehicles().get(0);
		this.currentCity = vehicle.homeCity();

		//long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
		long seed = -9019555669489983951L * currentCity.hashCode() * agent.id();
		this.random = new Random(seed);

		solution = new Solution(agent);
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		solution.addTaskToPlan(previous, winner, bids);
	}
	
	@Override
	public Long askPrice(Task task) {
		return solution.computeBid(task);
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		
		System.out.println("Agent " + agent.id() + " has tasks " + tasks);
		List<Plan> plans = new ArrayList<Plan>();
		List<AgentVehicle> Vehicles = solution.getPlayerVehicles();
		int len = Vehicles.size();
		for (int i=0; i<len; i++){
			plans.add(Vehicles.get(i).computePlan(Vehicles.get(i)));
		}

		//Plan planVehicle1 = naivePlan(vehicle, tasks);


		//plans.add(planVehicle1);
		//while (plans.size() < vehicles.size())
			//plans.add(Plan.EMPTY);

		return plans;
	}

	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		for (Task task : tasks) {
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity))
				plan.appendMove(city);

			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path())
				plan.appendMove(city);

			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
		}
		return plan;
	}
}
