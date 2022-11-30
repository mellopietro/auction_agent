package solution;

import logist.task.Task;
import logist.topology.Topology;

public class PlanStep {
    int action, capacity;
    long cost;
    Topology.City currentCity;
    Task dealtTask;

    public PlanStep(int action, long cost, int capacity, Topology.City currentCity, Task dealtTask) {
        this.action = action;
        this.cost = cost;
        this.capacity = capacity;
        this.currentCity = currentCity;
        this.dealtTask = dealtTask;
    }

    public int getAction() {
        return action;
    }

    public long getCost() {
        return cost;
    }

    public int getCapacity() {
        return capacity;
    }

    public Topology.City getCurrentCity() {
        return currentCity;
    }

    public Task getDealtTask() {
        return dealtTask;
    }
}
