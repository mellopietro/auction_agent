package solution;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology;

import java.util.LinkedList;
import java.util.List;

public class AgentVehicle {
    private int id;
    private final int totalCapacity;
    private final Topology.City homeCity;
    private final int costPerKm;
    private List<Task> tasks;
    private List<PlanStep> step;
    private long totalCost;

    public AgentVehicle(Vehicle vehicle) {
        this.id = vehicle.id();
        this.totalCapacity = vehicle.capacity();
        this.homeCity = vehicle.homeCity();
        this.costPerKm = vehicle.costPerKm();
        this.tasks = new LinkedList<>();
        this.step = new LinkedList<>();
        step.add(new PlanStep(0,0, totalCapacity, homeCity, null));
        step.add(new PlanStep(0,0, totalCapacity, null, null));
    }

    public AgentVehicle(int costPerKm) {
        this.totalCapacity = 1000;
        this.homeCity = null;
        this.costPerKm = costPerKm;
        this.tasks = new LinkedList<>();
        this.step = new LinkedList<>();
        step.add(new PlanStep(0,0, totalCapacity, null, null));
        step.add(new PlanStep(0,0, totalCapacity, null, null));
    }

    public TaskAssignment computeMarginalCost(Task task) {
        TaskAssignment bestAssignment = new TaskAssignment(id);
        int weight = task.weight;
        Topology.City pickupCity = task.pickupCity, deliveryCity = task.deliveryCity;
        int numberOfSteps = step.size()-1;
        for (int pick=0; pick<numberOfSteps; pick++) {
            PlanStep beforePickStep = step.get(pick), afterPickStep = step.get(pick+1);
            if (beforePickStep.capacity < weight) continue;
            long deltaPickCost = evaluateDeltaCost(pickupCity, beforePickStep, afterPickStep);
            long marginalCost = evaluatePDDeltaCost(pickupCity, deliveryCity, beforePickStep, afterPickStep);
            if (bestAssignment.marginalCost > marginalCost) {
                bestAssignment.marginalCost = marginalCost;
                bestAssignment.pickPosition = pick;
                bestAssignment.deliveryPosition = pick;
            }
            for (int delivery = pick+1; delivery<numberOfSteps; delivery++) {
                PlanStep beforeDeliveryStep = step.get(delivery), afterDeliveryStep = step.get(delivery+1);
                if (beforeDeliveryStep.capacity<weight) break;
                marginalCost = deltaPickCost + evaluateDeltaCost(deliveryCity, beforeDeliveryStep, afterDeliveryStep);
                if (bestAssignment.marginalCost > marginalCost) {
                    bestAssignment.marginalCost = marginalCost;
                    bestAssignment.pickPosition = pick;
                    bestAssignment.deliveryPosition = pick;
                }
            }
        }
        return bestAssignment;
    }

    private long evaluateDeltaCost(Topology.City city, PlanStep beforeStep, PlanStep afterStep) {
        long beforeCost = beforeStep.getCurrentCity() == null ? 0 : (long) (beforeStep.currentCity.distanceTo(city) * costPerKm);
        long afterCost = afterStep.getCurrentCity() == null ? 0 : (long) (city.distanceTo(afterStep.currentCity) * costPerKm);
        return beforeCost + afterCost - afterStep.cost;
    }

    private long evaluatePDDeltaCost(Topology.City pickCity, Topology.City deliveryCity, PlanStep beforeStep, PlanStep afterStep) {
        long beforeCost = beforeStep.getCurrentCity() == null ? 0 : (long) (beforeStep.currentCity.distanceTo(pickCity) * costPerKm);
        long deliveryCost = (long) pickCity.distanceTo(deliveryCity) * costPerKm;
        long afterCost = afterStep.getCurrentCity() == null ? 0 : (long) (deliveryCity.distanceTo(afterStep.currentCity) * costPerKm);
        return beforeCost + deliveryCost + afterCost - afterStep.cost;
    }

    public void addTaskToVehicle(Task task, TaskAssignment assignment) {
        if (assignment.vehicleId != id) return;
        PlanStep beforePickStep = step.get(assignment.pickPosition), afterPickStep = step.get(assignment.pickPosition+1);
        long beforePickCost = beforePickStep.getCurrentCity() == null ? 0 : (long) (beforePickStep.currentCity.distanceTo(task.pickupCity) * costPerKm);
        PlanStep pickStep = new PlanStep(1, beforePickCost, beforePickStep.capacity-task.weight, task.pickupCity, task);
        step.add(assignment.pickPosition+1, pickStep);
        afterPickStep.cost = afterPickStep.getCurrentCity() == null ? 0 : (long) (task.pickupCity.distanceTo(afterPickStep.currentCity) * costPerKm);

        for (int position = assignment.pickPosition+2; position<assignment.deliveryPosition+1; position++){
            step.get(position).capacity -= task.weight;
        }

        PlanStep beforeDeliveryStep = step.get(assignment.deliveryPosition+1), afterDeliveryStep = step.get(assignment.deliveryPosition+2);
        long beforeDeliveryCost = beforeDeliveryStep.getCurrentCity() == null ? 0 : (long) (beforeDeliveryStep.currentCity.distanceTo(task.deliveryCity) * costPerKm);
        PlanStep deliveryStep = new PlanStep(2, beforeDeliveryCost, beforeDeliveryStep.capacity+task.weight, task.deliveryCity, task);
        step.add(assignment.deliveryPosition+2, deliveryStep);
        afterDeliveryStep.cost = afterDeliveryStep.getCurrentCity() == null ? 0 : (long) (task.deliveryCity.distanceTo(afterDeliveryStep.currentCity) * costPerKm);

    }

    public Plan computePlan(AgentVehicle agentVehicle){

        int len = agentVehicle.step.size()-1;
        // città iniziale per il primo step che ha action 0
        Topology.City currentCity = agentVehicle.step.get(0).getCurrentCity();
        Plan plan = new Plan(currentCity);
        // piano per le altre azioni
        for (int i=1; i < len; i++){
            Topology.City nextCity = agentVehicle.step.get(i).getCurrentCity();
            Task task = agentVehicle.step.get(i).getDealtTask();
            for (Topology.City city : currentCity.pathTo(nextCity)) {
                plan.appendMove(city);
            }
            if (agentVehicle.step.get(i).getAction() == 1){
                plan.appendPickup(task);
                currentCity = task.pickupCity;
            }
            else{
                plan.appendDelivery(task);
                currentCity = task.deliveryCity;
            }
        }
        return plan;
    }


    public int getId() {
        return id;
    }

    public int getCapacity() {
        return totalCapacity;
    }

    public Topology.City getHomeCity() {
        return homeCity;
    }

    public int getCostPerKm() {
        return costPerKm;
    }

    public List<Task> getTasks() {
        return tasks;
    }

}
