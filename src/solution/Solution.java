package solution;

import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.task.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Solution {
        int costPerKm;
        private double growingFactor = 1;
        private int playerId;
        private List<List<AgentVehicle>> playersVehicles;
        private List<Long> playersTotalReward;
        private List<TaskAssignment> lastPlayerAssignment;

        public Solution(Agent player) {
                costPerKm = player.vehicles().stream().mapToInt(Vehicle::costPerKm).min().getAsInt();
                playerId = player.id();
                playersVehicles = new ArrayList<>();
                playersTotalReward = new ArrayList<>();
                for (int i=0; i<playerId; i++) {
                        playersVehicles.add(player.vehicles().stream().map(vehicle -> new AgentVehicle(costPerKm)).toList());
                        playersTotalReward.add(0L);
                }
                playersVehicles.add(player.vehicles().stream().map(AgentVehicle::new).toList());
                playersTotalReward.add(0L);
        }

        public long computeBid(Task task) {
                List<Integer> playersTaskNumber = new ArrayList<>();
                List<Double> playersMinBid = new ArrayList<>();

                lastPlayerAssignment = new ArrayList<>();
                int i=0;
                for (List<AgentVehicle> vehicles : playersVehicles) {
                        TaskAssignment bestAssignment = null;
                        for (AgentVehicle vehicle : vehicles) {
                                TaskAssignment currentAssignment = vehicle.computeMarginalCost(task);
                                if (bestAssignment == null || bestAssignment.marginalCost > currentAssignment.marginalCost) {
                                        bestAssignment = currentAssignment;
                                }
                        }
                        long cost = vehicles.stream().flatMap(agentVehicle -> agentVehicle.getStep().stream()).mapToLong(planStep -> planStep.cost).sum();
                        int taskNumber = vehicles.stream().mapToInt(agentVehicle -> agentVehicle.getTasks().size()).sum();
                        playersTaskNumber.add(taskNumber);
                        playersMinBid.add(taskNumber == 0 ? 0.8 * bestAssignment.marginalCost : bestAssignment.marginalCost * (1 + growingFactor + (double) playersTotalReward.get(i) / cost));
                        lastPlayerAssignment.add(bestAssignment);
                        i++;
                }

                int taskNumber = playersTaskNumber.stream().mapToInt(number -> number).sum();
                double averageBid = playersMinBid.stream().mapToDouble(bid -> bid).average().getAsDouble();

                double weight = 0.5;
                if (taskNumber > 6) {
                        weight = (0.5 + (double)(taskNumber - 6)/(taskNumber + 14));
                }

                return (long) ((1 - weight) * playersMinBid.get(playerId) + weight * averageBid);
        }

        public void addTaskToPlan(Task task, int winner, Long[] bids) {
                if (bids.length > playersVehicles.size()) {
                        int newAgents = bids.length-playersVehicles.size();
                        for (int i=0; i<newAgents; i++) {
                                playersVehicles.add(playersVehicles.get(playerId).stream().map(vehicle -> new AgentVehicle(costPerKm)).toList());
                                playersTotalReward.add(0L);
                        }
                }
                playersVehicles.get(winner).forEach(vehicle -> vehicle.addTaskToVehicle(task, lastPlayerAssignment.get(winner)));
                playersTotalReward.set(winner, playersTotalReward.get(winner) + bids[winner] - lastPlayerAssignment.get(winner).marginalCost);
        }

        public List<AgentVehicle> getPlayerVehicles(){
                return playersVehicles.get(playerId);
        }
}
