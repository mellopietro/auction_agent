package template;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology;

import java.util.List;

public class VehiclePlan {
    private List<Integer> actions;
    private List<Integer> weight;
    private double cost;

    public void addPickup(VehiclePlan plan, int position, double cost, int task_weight, int action){
        plan.actions.add(position, action);
        List<Integer> weights = plan.getWeight();
        int new_weight = weights.get(position-1) + task_weight;
        plan.weight.add(position,new_weight);
        int len = plan.actions.size();
        for (int i=position+1; i < len; i++){
            int var = plan.weight.get(i);
            plan.weight.set(i, var + task_weight);
        }
        plan.cost = cost;
    }
    public void addDelivery(VehiclePlan plan, int position, double cost, int task_weight, int action){
        plan.actions.add(position, action);
        List<Integer> weights = plan.getWeight();
        int new_weight = weights.get(position-1) - task_weight;
        plan.weight.add(position,new_weight);
        int len = plan.actions.size();
        for (int i=position+1; i < len; i++){
            int var = plan.weight.get(i);
            plan.weight.set(i, var - task_weight);
        }
        plan.cost = cost;
    }
    public double computeCost(VehiclePlan plan, List<Task> Tasks, Vehicle v){
        Topology.City current = v.getCurrentCity();
        double cost = 0;
        int len = plan.actions.size();
        int len2 = Tasks.size();
        int pos = 0;
        for (int i=0; i < len; i++){
            int id = plan.actions.get(i)%1000;
            if (plan.actions.get(i) < 500){
                // pick up
                // trovare task nella lista
                for (int j=0; j<len2; j++){
                    if (id == Tasks.get(j).id){
                        pos = j;
                        break;
                    }
                }
                Task task = Tasks.get(pos);
                cost = cost + current.distanceTo(task.pickupCity) * v.costPerKm();
                current = task.pickupCity;


            }
            else{
                // delivery
                // trovare task nella lista
                for (int j=0; j<len2; j++){
                    if (id == Tasks.get(j).id){
                        pos = j;
                        break;
                    }
                }
                Task task = Tasks.get(pos);
                cost = cost + current.distanceTo(task.deliveryCity) * v.costPerKm();
                current = task.deliveryCity;
            }
        }
        return cost;
    }

    public List<Integer> getWeight() {
        return weight;
    }
}
