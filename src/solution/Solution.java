package solution;

import logist.agent.Agent;
import logist.task.Task;

import java.util.LinkedList;
import java.util.List;

public class Solution {

        private double growingFactor = 0.1;
        private List<AgentVehicle> playerVehicles;
        private int playerId;
        private long playerCost;
        private List<AgentVehicle> opponentVehicles;
        private int opponentId;
        private long opponentCost;
        private TaskAssignment lastPlayerAssignment, lastOpponentAssignment;

        public Solution(Agent player) {
                playerVehicles = player.vehicles().stream().map(AgentVehicle::new).toList();
                opponentVehicles = new LinkedList<>();
                for (int i=0; i<playerVehicles.size(); i++) {
                       opponentVehicles.add(new AgentVehicle(playerVehicles.stream().mapToInt(AgentVehicle::getCostPerKm).min().getAsInt()));
                }
                playerId = player.id();
                playerCost = 0;
                opponentCost = 0;
        }

        public long computeBid(Task task) {
                lastPlayerAssignment = null; lastOpponentAssignment = null;
                for (AgentVehicle vehicle : playerVehicles) {
                       TaskAssignment currentAssignment = vehicle.computeMarginalCost(task);
                       if (lastPlayerAssignment == null || lastPlayerAssignment.marginalCost > currentAssignment.marginalCost) {
                               lastPlayerAssignment = currentAssignment;
                       }
                }
                for (AgentVehicle vehicle : opponentVehicles) {
                        TaskAssignment currentAssignment = vehicle.computeMarginalCost(task);
                        if (lastOpponentAssignment == null || lastOpponentAssignment.marginalCost > currentAssignment.marginalCost) {
                                lastOpponentAssignment = currentAssignment;
                        }
                }
                int taskNumber = playerVehicles.stream().mapToInt(agentVehicle -> agentVehicle.getTasks().size()).sum();
                assert lastPlayerAssignment != null;
                return (long) (lastPlayerAssignment.marginalCost * (1+growingFactor * taskNumber));
        }

        public void addTaskToPlan(Task task, int winner, Long[] bids) {
                if (playerId == winner) {
                        playerVehicles.forEach(vehicle -> vehicle.addTaskToVehicle(task, lastPlayerAssignment));
                        playerCost += bids[winner];
                } else {
                        opponentVehicles.forEach(vehicle -> vehicle.addTaskToVehicle(task, lastOpponentAssignment));
                        opponentCost += bids[winner];
                }
        }
}
