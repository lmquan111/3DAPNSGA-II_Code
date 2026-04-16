import java.util.List;

public class ThreeDAP {
    private final double LAMBDA = 0.05;
    private int customerAmount;
    private int depotAmount;
    //C x D -> distance from each customer to each depot


    private List<StaticCustomer> staticCustomers;
    private List<Depot> depots;


    public ThreeDAP(List<StaticCustomer> staticCustomers, List<Depot> depots) {
        this.staticCustomers = staticCustomers;
        this.depots = depots;
        customerAmount = staticCustomers.size();
        depotAmount = depots.size();
    }

    public void reCluster() {
        int M = 100;
        double[][] S = new double[customerAmount][depotAmount];
        double[][] A = new double[customerAmount][depotAmount]; // Initialize to 0
        double[][] R = new double[customerAmount][depotAmount]; // Initialize to 0

        STD(S); // Fill the Similarity matrix

        for (int m = 0; m < M; m++) {
            // Update Responsibility R
            for (int i = 0; i < customerAmount; i++) {
                for (int k = 0; k < depotAmount; k++) {
                    // Find max(S[i, k'] + A[i, k']) where k' != k
                    double max = -Double.MAX_VALUE;
                    for (int kp = 0; kp < depotAmount; kp++) {
                        if (kp != k) {
                            max = Math.max(max, S[i][kp] + A[i][kp]);
                        }
                    }
                    double newR = S[i][k] - max;
                    R[i][k] = (1 - LAMBDA) * newR + (LAMBDA * R[i][k]); // Damping
                }
            }

            // Update Availability A
            for (int k = 0; k < depotAmount; k++) {
                // Sum of positive responsibilities for depot k from other customers
                double sumR = 0;
                for (int ip = 0; ip < customerAmount; ip++) {
                    sumR += Math.max(0, R[ip][k]);
                }

                for (int i = 0; i < customerAmount; i++) {
                    // a(i, k) = min(0, r(k, k) + sum_{i' s.t. i' \neq i} max(0, r(i', k)))
                    // Simplified for fixed depots:
                    double newA = sumR - Math.max(0, R[i][k]);
                    if (newA > 0) newA = 0; // Usually A is non-positive in standard AP

                    A[i][k] = (1 - LAMBDA) * newA + (LAMBDA * A[i][k]); // Damping
                }
            }
        }

        //Assign Cluster IDs
        for (int i = 0; i < customerAmount; i++) {
            double maxCriterionSameType = -Double.MAX_VALUE;
            int bestDepotSameType = -1;
            int bestDepotDifferentType = -1;
            double maxCriterionDifferentType = -Double.MAX_VALUE;

            for (int k = 0; k < depotAmount; k++) {
                // Match types: Delivery customer to Delivery depot, Pickup to Pickup
                if (staticCustomers.get(i).getType().toString().equals(depots.get(k).getType().toString())) {
                    double criterion = R[i][k] + A[i][k];
                    if (criterion > maxCriterionSameType) {
                        maxCriterionSameType = criterion;
                        bestDepotSameType = k;
                    }
                }
            }
            // Update the customer object with the assigned Depot ID or Index
            if (bestDepotSameType != -1) {
                staticCustomers.get(i).setClusterId(bestDepotSameType);
            }

            for (int k = 0; k < depotAmount; k++) {
                // Match types: Delivery customer to Delivery depot, Pickup to Pickup
                if (!staticCustomers.get(i).getType().toString().equals(depots.get(k).getType().toString())) {
                    double criterion = R[i][k] + A[i][k];
                    if (criterion > maxCriterionDifferentType) {
                        maxCriterionDifferentType = criterion;
                        bestDepotDifferentType = k;
                    }
                }
            }
            // Update the customer object with the assigned Depot ID or Index
            if (bestDepotDifferentType != -1) {
                staticCustomers.get(i).setClusterId2(bestDepotDifferentType);
            }
        }
    }

    public void STD(double[][] S) {
        for (int i = 0; i < customerAmount; i++) {
            StaticCustomer customer = staticCustomers.get(i);
            for (int j = 0; j < depotAmount; j++) {
                Depot depot = depots.get(j);

                // If types don't match, similarity is extremely low (negative infinity)
                if (!customer.getType().toString().equals(depot.getType().toString())) {
                    S[i][j] = -1e10;
                    continue;
                }

                double li = customer.getStartTimeWindow(), ri = customer.getEndTimeWindow();
                double lj = depot.getStartTimeWindow(), rj = depot.getEndTimeWindow();

                double timeDiff = Math.min(Math.min(Math.abs(li - lj), Math.abs(li - rj)),
                        Math.min(Math.abs(ri - lj), Math.abs(ri - rj)));

                double s = 0.9;
                double t = 0.1;
                double std = (s * distanceCalculate(i, j)) + (t * timeDiff/60 * Constants.alpha_v);
                S[i][j] = -std;
            }
        }
    }

    public double distanceCalculate(int i , int j){//i for customer, j for depot
        return Math.sqrt((staticCustomers.get(i).getX() - depots.get(j).getX())
                *(staticCustomers.get(i).getX() - depots.get(j).getX())
                +(staticCustomers.get(i).getY() - depots.get(j).getY())
                *(staticCustomers.get(i).getY() - depots.get(j).getY()));
    }

}