import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        
        List<Depot> depots = new ArrayList<>();
        List<StaticCustomer> staticCustomers = new ArrayList<>();
        List<DynamicCustomer> dynamicCustomers = new ArrayList<>();

        
        String csvFile = "16 Instance16.csv";
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))){
            
            
            br.readLine(); 

            
            while ((line = br.readLine()) != null){
                String[] data = line.split(cvsSplitBy);

                
                int id = Integer.parseInt(data[0]);
                double x = Double.parseDouble(data[1]);
                double y = Double.parseDouble(data[2]);
                double deliveryDemand = Double.parseDouble(data[3]);
                double pickupDemand = Double.parseDouble(data[4]);
                double twLeftD = Double.parseDouble(data[5]);
                double twRightD = Double.parseDouble(data[6]);
                double twLeftP = Double.parseDouble(data[7]);
                double twRightP = Double.parseDouble(data[8]);
                double knownTime = Double.parseDouble(data[9]);
                int dcs = Integer.parseInt(data[10]);
                int pcs = Integer.parseInt(data[11]);

                
                if (dcs >= 1000 || pcs >= 1000) {
                    if (dcs >= 1000) {
                        depots.add(new Depot(id, x, y, DepotType.DELIVERY, twLeftD, twRightD));
                    } 
                    else {
                        depots.add(new Depot(id, x, y, DepotType.PICKUP, twLeftP, twRightP));
                    }
                } 
                
                else{
                    CustomerType type;
                    double demand;
                    double startTimeWindow;
                    double endTimeWindow;

                    int tmpCluster = -1;
                    
                    if (deliveryDemand > 0){
                        type = CustomerType.DELIVERY;
                        demand = deliveryDemand;
                        startTimeWindow = twLeftD;
                        endTimeWindow = twRightD;

                        if(dcs == 1){
                            tmpCluster = 0;
                        }
                        else if(dcs == 2){
                            tmpCluster = 1;
                        }

                    } 
                    else{
                        type = CustomerType.PICKUP;
                        demand = pickupDemand;
                        startTimeWindow = twLeftP;
                        endTimeWindow = twRightP;

                        if(pcs == 1){
                            tmpCluster = 2;
                        }
                        else if(pcs == 2){
                            tmpCluster = 3;
                        }
  
                    }

                    
                    if (knownTime == 0){
                        // staticCustomers.add(new StaticCustomer(id, x, y, demand, type, startTimeWindow, endTimeWindow, -1));
                        staticCustomers.add(new StaticCustomer(id, x, y, demand, type, startTimeWindow, endTimeWindow, tmpCluster));
                    } 
                    else{
                        dynamicCustomers.add(new DynamicCustomer(id, x, y, demand, type, startTimeWindow, endTimeWindow, knownTime));
                    }
                }
            }

        } 
        catch (IOException e){
            e.printStackTrace();
        }


        System.out.println("=== DANH SÁCH KHO (" + depots.size() + ") ===");
        for (Depot d : depots) {
            System.out.println(d.toString());
        }

        System.out.println("\n=== DANH SÁCH KHÁCH TINH (" + staticCustomers.size() + ") ===");
        for (int i = 0; i < staticCustomers.size(); i++) {
            System.out.println(staticCustomers.get(i).toString());
        }

        System.out.println("\n=== DANH SÁCH KHÁCH ĐONG (" + dynamicCustomers.size() + ") ===");
        for (DynamicCustomer dc : dynamicCustomers) {
            System.out.println(dc.toString());
        }
    }
}