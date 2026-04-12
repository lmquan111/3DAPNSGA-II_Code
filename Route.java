// Sub route của một lời giải

import java.util.ArrayList;
import java.util.List;

public class Route {
    public List<Integer> encodedCustomers;
    public Depot startDepot;
    public Depot endDepot;
    public double deliveryLoad; 
    public double pickupLoad;   

    public Route() {
        encodedCustomers = new ArrayList<>();
        deliveryLoad = 0;
        pickupLoad = 0;
    }
}
