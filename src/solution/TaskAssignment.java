package solution;

public class TaskAssignment {
    public int pickPosition, deliveryPosition, vehicleId;
    public long marginalCost;

    public TaskAssignment(int vehicleId) {
        this.vehicleId = vehicleId;
        pickPosition = -1;
        deliveryPosition = -1;
        marginalCost = 100000000;
    }
}
