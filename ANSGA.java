import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.LinkedList;

public class ANSGA {
    
    private int Run;
    private int numTimeSlot;
    private double timeHorizon;
    private List<Individual> population;
    
    private List<StaticCustomer> staticCustomers;
    private List<DynamicCustomer> dynamicCustomers;
    private List<Depot> depots;

    public ANSGA(int Run, int numTimeSlot, double timeHorizon, 
                 List<StaticCustomer> staticCustomers, List<DynamicCustomer> dynamicCustomers, List<Depot> depots){
        
        this.Run = Run;
        this.numTimeSlot = numTimeSlot;
        this.timeHorizon = timeHorizon;
        this.staticCustomers = staticCustomers;
        this.dynamicCustomers = dynamicCustomers;
        this.depots = depots;
        
        this.population = new ArrayList<>();
        
        initializePopulation();
    }

    private void initializePopulation() {        
        
        Map<Integer, List<Integer>> clusterMap = new HashMap<>();
        
        for (int i = 0; i < staticCustomers.size(); i++) {
            int clusterId = staticCustomers.get(i).getClusterId();
            clusterMap.putIfAbsent(clusterId, new ArrayList<>());
            
            clusterMap.get(clusterId).add(i); 
        }
        
        List<Integer> clusterIds = new ArrayList<>(clusterMap.keySet());
        
        for (int ind = 0; ind < Constants.N_ind; ind++) {
            List<Integer> chromosome = new ArrayList<>();
            
            List<Integer> shuffledClusterIds = new ArrayList<>(clusterIds);
            Collections.shuffle(shuffledClusterIds);
            
            for (int clusterId : shuffledClusterIds) {
            
                List<Integer> shuffledIndices = new ArrayList<>(clusterMap.get(clusterId));
                Collections.shuffle(shuffledIndices);
                
                chromosome.addAll(shuffledIndices);
            }
            
            population.add(new Individual(chromosome));
        }
        
    }

    List<Individual> runAlgorithm() {

        List<List<DynamicCustomer>> d = new ArrayList<>();
        for (int t = 0; t < numTimeSlot; t++) {
            d.add(new ArrayList<>());
        }
        double slotDuration = timeHorizon / numTimeSlot;
        for (DynamicCustomer dc : dynamicCustomers) {
            int slotIndex = (int) (dc.getKnownTime() / slotDuration);
            if (slotIndex >= numTimeSlot) slotIndex = numTimeSlot - 1;
            d.get(slotIndex).add(dc);
        }


        int cnt = 0;
        List<Individual> gbest = new ArrayList<>(); 
        
        int Rlg = Constants.Rlg;
        LinkedList<List<Individual>> SgBest = new LinkedList<>(); 

        for (int gen = 1; gen <= this.Run; gen++) {
            
            List<Individual> R = applyCrossoverAndMutation(this.population);
            
            List<Individual> Q = new ArrayList<>();
            Q.addAll(this.population);
            Q.addAll(R);
            
            for (Individual pop : Q) {
                Individual copyPop = pop.clone(); 
                
                for (int t = 0; t < numTimeSlot; t++) {
                    List<DynamicCustomer> Ds = d.get(t);
                    if (!Ds.isEmpty()) {
                        insertionStrategy(copyPop, Ds); 
                    }
                }
                
                evaluate(copyPop);
                
                pop.setNV(copyPop.getNV());
                pop.setTOC(copyPop.getTOC());
            }
            
            this.population = nonDominatedSort(Q, Constants.N_ind);
            
            List<Individual> rank1 = getRank1(this.population);
            
            if (cnt == 0) {
                gbest = new ArrayList<>(rank1);
            } 
            else {
                double hvRank1 = calculateHV(rank1);
                double hvGbest = calculateHV(gbest);
                
                if (hvRank1 > hvGbest) {
                    gbest = new ArrayList<>(rank1);
                }
                
                SgBest.add(new ArrayList<>(gbest));

                if (SgBest.size() > Rlg) {
                    SgBest.poll(); 
                }
                
                double currentHv = calculateHV(SgBest.getLast());
                boolean check = false;
                
                if (SgBest.size() == Rlg) {
                    for (int x = 1; x < Rlg; x++) {
                        double pastHv = calculateHV(SgBest.get(SgBest.size() - 1 - x));
                        if (Math.abs(currentHv - pastHv) > 0.01) {
                            check = true;
                            break;
                        }
                    }
                } 
                else {
                    check = true;
                }

                if (!check) { 
                    List<Individual> ngbest = applyLNS(gbest, Constants.Rc);
                    
                    double hvNgbest = calculateHV(ngbest);

                    if (hvNgbest > calculateHV(gbest)) { 
                        gbest = new ArrayList<>(ngbest);
                    }
                    
                    replaceRank1(this.population, ngbest);
                }
            }
            
            cnt++;
            // System.out.println("Hoàn thành Generation: " + gen + " | Current gbest HV: " + calculateHV(gbest));
        }
        return gbest;
    }


    private void replaceRank1(List<Individual> pop, List<Individual> newRank1) {
        
        pop.removeIf(ind -> ind.getRank() == 1);

        for (Individual ind : newRank1) {
            Individual newElite = ind.clone();
            newElite.setRank(1);               
            pop.add(newElite);
        }

        if (pop.size() > Constants.N_ind) {

            pop.sort((i1, i2) -> {
                if (i1.getRank() != i2.getRank()) {
                    return Integer.compare(i1.getRank(), i2.getRank()); 
                }

                return Double.compare(i2.getCrowdingDistance(), i1.getCrowdingDistance()); 
            });

            while (pop.size() > Constants.N_ind) {
                pop.remove(pop.size() - 1);
            }
        } 

    }


    /*
        calculateHV
    */

    private double calculateHV(List<Individual> paretoFront) {
        if (paretoFront == null || paretoFront.isEmpty()) {
            return 0.0;
        }

        // scaling
        double minNV = Double.MAX_VALUE, maxNV = -Double.MAX_VALUE;
        double minTOC = Double.MAX_VALUE, maxTOC = -Double.MAX_VALUE;

        for (Individual ind : paretoFront) {
            if (ind.getNV() < minNV) minNV = ind.getNV();
            if (ind.getNV() > maxNV) maxNV = ind.getNV();
            if (ind.getTOC() < minTOC) minTOC = ind.getTOC();
            if (ind.getTOC() > maxTOC) maxTOC = ind.getTOC();
        }

        
        double rangeNV = (maxNV - minNV == 0) ? 1.0 : (maxNV - minNV);
        double rangeTOC = (maxTOC - minTOC == 0) ? 1.0 : (maxTOC - minTOC);

        List<Individual> sortedFront = new ArrayList<>(paretoFront);
        sortedFront.sort((i1, i2) -> {
            int nvCmp = Integer.compare(i1.getNV(), i2.getNV());
            if (nvCmp != 0) return nvCmp;
            return Double.compare(i1.getTOC(), i2.getTOC());
        });


        double refNV = 1.1; 
        double refTOC = 1.1;
        double hv = 0.0;

        for (int i = 0; i < sortedFront.size(); i++) {
            Individual current = sortedFront.get(i);
            
            double normNV_current = (current.getNV() - minNV) / rangeNV;
            double normTOC_current = (current.getTOC() - minTOC) / rangeTOC;

            double normNV_next = (i == sortedFront.size() - 1) ? refNV : (sortedFront.get(i + 1).getNV() - minNV) / rangeNV;

            double width = normNV_next - normNV_current;
            double height = refTOC - normTOC_current; 

            if (width > 0 && height > 0) {
                hv += width * height;
            }
        }

        return hv;
    }

    /*
    LNS
    */

    private List<Individual> applyLNS(List<Individual> gbest, int destroyCount) {
        List<Individual> improvedGbest = new ArrayList<>();
        Random rand = new Random();

        for (Individual pop : gbest) {
            Individual newInd = pop.clone(); 
            List<Integer> chromosome = newInd.getChromosome();

            List<Integer> removedCustomers = new ArrayList<>();

            int numToDestroy = Math.min(destroyCount, chromosome.size());

            for (int i = 0; i < numToDestroy; i++) {
                int removeIndex = rand.nextInt(chromosome.size());
                removedCustomers.add(chromosome.remove(removeIndex));
            }

            for (int customerId : removedCustomers) {
                double bestCost = Double.MAX_VALUE;
                int bestInsertPos = -1;
                
                Individual tempInd = new Individual();

                for (int pos = 0; pos <= chromosome.size(); pos++) {
                    chromosome.add(pos, customerId);
                    
                    tempInd.setChromosome(chromosome);
                    evaluate(tempInd); 
                    
                    double currentCost = tempInd.getNV() * 100000.0 + tempInd.getTOC();

                    if (currentCost < bestCost) {
                        bestCost = currentCost;
                        bestInsertPos = pos;
                    }

                    chromosome.remove(pos);
                }

                if (bestInsertPos != -1) {
                    chromosome.add(bestInsertPos, customerId);
                } 
                else {
                    chromosome.add(customerId);
                }
            }

            newInd.setChromosome(chromosome);
            evaluate(newInd); 
            
            improvedGbest.add(newInd);
        }

        return improvedGbest;
    }


    /*

        non-dominated sorting
    
    */
    private boolean dominates(Individual p, Individual q) {
        // p trội hơn q nếu p <= q ở mọi mục tiêu VÀ p < q ở ít nhất 1 mục tiêu
        boolean betterOrEqual = (p.getNV() <= q.getNV()) && (p.getTOC() <= q.getTOC());
        boolean strictlyBetter = (p.getNV() < q.getNV()) || (p.getTOC() < q.getTOC());
        
        return betterOrEqual && strictlyBetter;
    }

    private List<Individual> nonDominatedSort(List<Individual> combinedPop, int targetSize) {
        List<List<Individual>> fronts = new ArrayList<>();
        List<Individual> front1 = new ArrayList<>();

        Map<Individual, List<Individual>> S = new HashMap<>(); 
        Map<Individual, Integer> n = new HashMap<>();          

        for (Individual p : combinedPop) {
            S.put(p, new ArrayList<>());
            n.put(p, 0);

            for (Individual q : combinedPop) {
                if (dominates(p, q)) {
                    S.get(p).add(q); 
                } 
                else if (dominates(q, p)) {
                    n.put(p, n.get(p) + 1); 
                }
            }

            if (n.get(p) == 0) {
                p.setRank(1);
                front1.add(p);
            }
        }

        fronts.add(front1);
        int currentFrontIndex = 0;


        while (!fronts.get(currentFrontIndex).isEmpty()) {
            List<Individual> nextFront = new ArrayList<>();
            for (Individual p : fronts.get(currentFrontIndex)) {
                for (Individual q : S.get(p)) {
                    n.put(q, n.get(q) - 1);
                    if (n.get(q) == 0) {
                        q.setRank(currentFrontIndex + 2); 
                        nextFront.add(q);
                    }
                }
            }
            currentFrontIndex++;
            if (!nextFront.isEmpty()) {
                fronts.add(nextFront);
            } 
            else {
                break; 
            }
        }


        List<Individual> newPopulation = new ArrayList<>();
        
        for (List<Individual> front : fronts) {
            if (front.isEmpty()) continue;
            
            calculateCrowdingDistance(front);

            if (newPopulation.size() + front.size() <= targetSize) {
                newPopulation.addAll(front);
            } 
            else {
                
                front.sort((i1, i2) -> Double.compare(i2.getCrowdingDistance(), i1.getCrowdingDistance()));
                
                int remain = targetSize - newPopulation.size();
                for (int i = 0; i < remain; i++) {
                    newPopulation.add(front.get(i));
                }
                break; 
            }
        }

        return newPopulation;
    }

    private void calculateCrowdingDistance(List<Individual> front) {
        int size = front.size();
        if (size == 0) return;
        
        for (Individual ind : front) {
            ind.setCrowdingDistance(0.0);
        }
        
        if (size <= 2) {
            for (Individual ind : front) {
                ind.setCrowdingDistance(Double.POSITIVE_INFINITY);
            }
            return;
        }

        front.sort((i1, i2) -> Integer.compare(i1.getNV(), i2.getNV()));
        front.get(0).setCrowdingDistance(Double.POSITIVE_INFINITY);
        front.get(size - 1).setCrowdingDistance(Double.POSITIVE_INFINITY);
        
        double minNV = front.get(0).getNV();
        double maxNV = front.get(size - 1).getNV();
        double rangeNV = maxNV - minNV == 0 ? 1 : maxNV - minNV;

        for (int i = 1; i < size - 1; i++) {
            double currentDist = front.get(i).getCrowdingDistance();
            double dist = (front.get(i + 1).getNV() - front.get(i - 1).getNV()) / rangeNV;
            front.get(i).setCrowdingDistance(currentDist + dist);
        }

        front.sort((i1, i2) -> Double.compare(i1.getTOC(), i2.getTOC()));
        front.get(0).setCrowdingDistance(Double.POSITIVE_INFINITY);
        front.get(size - 1).setCrowdingDistance(Double.POSITIVE_INFINITY);
        
        double minTOC = front.get(0).getTOC();
        double maxTOC = front.get(size - 1).getTOC();
        double rangeTOC = maxTOC - minTOC == 0 ? 1 : maxTOC - minTOC;

        for (int i = 1; i < size - 1; i++) {
            if (front.get(i).getCrowdingDistance() != Double.POSITIVE_INFINITY) {
                double currentDist = front.get(i).getCrowdingDistance();
                double dist = (front.get(i + 1).getTOC() - front.get(i - 1).getTOC()) / rangeTOC;
                front.get(i).setCrowdingDistance(currentDist + dist);
            }
        }
    }

    private List<Individual> getRank1(List<Individual> pop) {
        List<Individual> rank1 = new ArrayList<>();
        for (Individual ind : pop) {
            if (ind.getRank() == 1) {
                rank1.add(ind.clone()); 
            }
        }
        return rank1;
    }

    /*
        insertion strategy
    */
   private double calculateRouteCost(Route route) {
        double TC = 0, PC = 0, FC = 0, IC = 0;
        
        double currentTime = route.startDepot.getStartTimeWindow();
        double currentX = route.startDepot.getX();
        double currentY = route.startDepot.getY();

        for (int encodedId : route.encodedCustomers) {
            Object c = getCustomerObject(encodedId);
            double cx = (c instanceof StaticCustomer) ? ((StaticCustomer) c).getX() : ((DynamicCustomer) c).getX();
            double cy = (c instanceof StaticCustomer) ? ((StaticCustomer) c).getY() : ((DynamicCustomer) c).getY();
            
            double demand = getDemand(encodedId);
            double twLeft = getStartTimeWindow(encodedId);   
            double twRight = getEndTimeWindow(encodedId); 
            CustomerType type = getType(encodedId);

            double dist = calculateDistance(currentX, currentY, cx, cy);
            TC += dist * Constants.fv * Constants.pv;
            
            double arrivalTime = currentTime + (dist / Constants.alpha_v);

            double early = Math.max(twLeft - arrivalTime, 0); 
            double late = Math.max(arrivalTime - twRight, 0); 
            PC += (Constants.epsilon * early) + (Constants.omega * late);

            currentTime = Math.max(arrivalTime, twLeft);

            if (type == CustomerType.DELIVERY) {
                FC += demand * Constants.delta; 
            } 
            else {
                FC += demand * Constants.khi;   
            }

            if (encodedId >= staticCustomers.size()) {
                IC += demand * Constants.gamma;
            }

            currentX = cx;
            currentY = cy;
        }

        double distToDepot = calculateDistance(currentX, currentY, route.endDepot.getX(), route.endDepot.getY());
        TC += distToDepot * Constants.fv * Constants.pv;

        return TC + PC + FC + IC;
    }

    private void evaluate(Individual ind) {
        List<Route> routes = splitChromosome(ind.getChromosome());
        
        ind.setNV(routes.size());

        double totalTOC = 0;
        
        double MC = routes.size() * (Constants.Mv / Constants.T);
        totalTOC += MC;

        for (Route route : routes) {
            totalTOC += calculateRouteCost(route);
        }

        ind.setTOC(totalTOC);
    }

    private List<Route> splitChromosome(List<Integer> chromosome) {
        List<Route> routes = new ArrayList<>();
        Route currentRoute = new Route();

        for (int i = 0; i < chromosome.size(); i++) {
            int encodedId = chromosome.get(i);
            double demand = getDemand(encodedId);
            CustomerType type = getType(encodedId);

            boolean needSplit = false;

            if(currentRoute.deliveryLoad + currentRoute.pickupLoad + demand > Constants.d_v) needSplit = true;
            // if (type == CustomerType.DELIVERY && (currentRoute.deliveryLoad + demand > Constants.d_v)) {
            //     needSplit = true;
            // } 
            // else if (type == CustomerType.PICKUP && (currentRoute.pickupLoad + demand > Constants.d_v)) {
            //     needSplit = true;
            // }

            
            if (!currentRoute.encodedCustomers.isEmpty()) {
                CustomerType firstTypeInRoute = getType(currentRoute.encodedCustomers.get(0));
                if (firstTypeInRoute == CustomerType.PICKUP && type == CustomerType.DELIVERY) {
                    needSplit = true;
                }
            }

            if (needSplit) {
                if (!currentRoute.encodedCustomers.isEmpty()) {
                    assignDepotsToRoute(currentRoute);
                    routes.add(currentRoute);
                }
                currentRoute = new Route();
            }

            
            currentRoute.encodedCustomers.add(encodedId);
            if (type == CustomerType.DELIVERY) {
                currentRoute.deliveryLoad += demand;
            } 
            else {
                currentRoute.pickupLoad += demand;
            }
        }

        if (!currentRoute.encodedCustomers.isEmpty()) {
            assignDepotsToRoute(currentRoute);
            routes.add(currentRoute);
        }

        return routes;
    }

    private void assignDepotsToRoute(Route route) {
        List<Integer> customers = route.encodedCustomers;
        
        
        CustomerType firstType = getType(customers.get(0));
        CustomerType lastType = getType(customers.get(customers.size() - 1));

        
        if (firstType == CustomerType.DELIVERY) {
            route.startDepot = findNearestDepot(customers.get(0), DepotType.DELIVERY);
        } 
        else {
            route.startDepot = findNearestDepot(customers.get(0), DepotType.PICKUP);
        }

        if (lastType == CustomerType.DELIVERY) {
            route.endDepot = findNearestDepot(customers.get(customers.size() - 1), DepotType.DELIVERY);
        } 
        else {
            route.endDepot = findNearestDepot(customers.get(customers.size() - 1), DepotType.PICKUP);
        }
    }


    private Depot findNearestDepot(int encodedCustomerId, DepotType requiredType) {
        Object c = getCustomerObject(encodedCustomerId);
        
        // tĩnh
        if (c instanceof StaticCustomer) {
            int clusterId = ((StaticCustomer) c).getClusterId();
            return depots.get(clusterId); 
        } 
        // động tạo đường mới
        else {
            double cx = ((DynamicCustomer) c).getX();
            double cy = ((DynamicCustomer) c).getY();

            Depot nearest = null;
            double minDistance = Double.MAX_VALUE;

            for (Depot d : depots) {
                if (d.getType() == requiredType) {
                    double dist = calculateDistance(cx, cy, d.getX(), d.getY());
                    if (dist < minDistance) {
                        minDistance = dist;
                        nearest = d;
                    }
                }
            }
            return nearest;
        }
    }

    private void insertionStrategy(Individual copyPop, List<DynamicCustomer> dynamics) {
        List<Integer> chromosome = copyPop.getChromosome();

        for (DynamicCustomer dc : dynamics) {

            int dynamicEncodedId = staticCustomers.size() + dynamicCustomers.indexOf(dc);
            List<Route> routes = splitChromosome(chromosome);

            double bestCostIncrease = Double.MAX_VALUE;
            int bestInsertIndexInChromosome = -1;
            int globalGenIndex = 0; 

            for (Route route : routes) {
                boolean canInsert = true;

                if (route.deliveryLoad + route.pickupLoad + dc.getDemand() > Constants.d_v) {
                    canInsert = false;
                }

                if (canInsert) {
                    double oldRouteCost = calculateRouteCost(route);

                    for (int pos = 0; pos <= route.encodedCustomers.size(); pos++) {
                        
                        Route tempRoute = new Route();
                        tempRoute.startDepot = route.startDepot; 
                        tempRoute.endDepot = route.endDepot;     
                        tempRoute.encodedCustomers = new ArrayList<>(route.encodedCustomers);
                        
                        tempRoute.encodedCustomers.add(pos, dynamicEncodedId);

                        double newRouteCost = calculateRouteCost(tempRoute);
                        double costIncrease = newRouteCost - oldRouteCost;

                        if (costIncrease < bestCostIncrease) {
                            bestCostIncrease = costIncrease;
                            bestInsertIndexInChromosome = globalGenIndex + pos;
                        }
                    }
                }
                
                globalGenIndex += route.encodedCustomers.size();
            }

            if (bestInsertIndexInChromosome != -1) {
                chromosome.add(bestInsertIndexInChromosome, dynamicEncodedId);
            } 
            else {
                chromosome.add(dynamicEncodedId);
            }
        }

        copyPop.setChromosome(chromosome);
    }

    /*
    
    
    */

    private List<Individual> applyCrossoverAndMutation(List<Individual> pop) {
        List<Individual> offspring = new ArrayList<>();
        Random rand = new Random();

        
        while (offspring.size() < Constants.N_ind) {
            
            Individual parent1 = tournamentSelection(pop, rand);
            Individual parent2 = tournamentSelection(pop, rand);

            List<Integer> chrom1 = parent1.getChromosome();
            List<Integer> chrom2 = parent2.getChromosome();

            List<Integer> childChrom1, childChrom2;

            if (rand.nextDouble() < Constants.cp) {
                List<List<Integer>> children = pmxCrossover(chrom1, chrom2, rand);
                childChrom1 = children.get(0);
                childChrom2 = children.get(1);
            } 
            else {
                childChrom1 = new ArrayList<>(chrom1);
                childChrom2 = new ArrayList<>(chrom2);
            }

            swapMutation(childChrom1, rand);
            swapMutation(childChrom2, rand);

            offspring.add(new Individual(childChrom1));

            if (offspring.size() < Constants.N_ind) {
                offspring.add(new Individual(childChrom2));
            }
        }
        return offspring;
    }

    private Individual tournamentSelection(List<Individual> pop, Random rand) {
        Individual ind1 = pop.get(rand.nextInt(pop.size()));
        Individual ind2 = pop.get(rand.nextInt(pop.size()));

        boolean ind1DominatesInd2 = (ind1.getNV() <= ind2.getNV() && ind1.getTOC() <= ind2.getTOC()) && 
                                    (ind1.getNV() < ind2.getNV() || ind1.getTOC() < ind2.getTOC());

        boolean ind2DominatesInd1 = (ind2.getNV() <= ind1.getNV() && ind2.getTOC() <= ind1.getTOC()) && 
                                    (ind2.getNV() < ind1.getNV() || ind2.getTOC() < ind1.getTOC());

        if (ind1DominatesInd2) {
            return ind1; 
        } 
        else if (ind2DominatesInd1) {
            return ind2; 
        } 
        else {
            return rand.nextBoolean() ? ind1 : ind2;
        }
    }

    private List<List<Integer>> pmxCrossover(List<Integer> p1, List<Integer> p2, Random rand) {
        int size = p1.size();
        int point1 = rand.nextInt(size);
        int point2 = rand.nextInt(size);

        if (point1 > point2) {
            int temp = point1; point1 = point2; point2 = temp;
        }

        List<Integer> c1 = new ArrayList<>(Collections.nCopies(size, -1));
        List<Integer> c2 = new ArrayList<>(Collections.nCopies(size, -1));

        for (int i = point1; i <= point2; i++){
            c1.set(i, p1.get(i));
            c2.set(i, p2.get(i));
        }

        fillPMXRest(c1, p1, p2, point1, point2, size);
        fillPMXRest(c2, p2, p1, point1, point2, size);

        List<List<Integer>> result = new ArrayList<>();
        result.add(c1);
        result.add(c2);
        return result;
    }

    private void fillPMXRest(List<Integer> child, List<Integer> parent1, List<Integer> parent2, int p1, int p2, int size) {
        for (int i = 0; i < size; i++) {
            if (i >= p1 && i <= p2) continue; 

            int val = parent2.get(i);
            
            while (child.subList(p1, p2 + 1).contains(val)) {
                int indexInParent1 = parent1.indexOf(val);
                val = parent2.get(indexInParent1);
            }
            child.set(i, val);
        }
    }

    private void swapMutation(List<Integer> chromosome, Random rand) {
        if (rand.nextDouble() < Constants.mp) {
            int idx1 = rand.nextInt(chromosome.size());
            int idx2 = rand.nextInt(chromosome.size());
            Collections.swap(chromosome, idx1, idx2);
        }
    }

    /*
        Helper 
    */
   private Object getCustomerObject(int encodedId) {
        if (encodedId < staticCustomers.size()) {
            return staticCustomers.get(encodedId);
        } 
        else {
            return dynamicCustomers.get(encodedId - staticCustomers.size());
        }
    }

    private double getDemand(int encodedId) {
        Object c = getCustomerObject(encodedId);
        if (c instanceof StaticCustomer) return ((StaticCustomer) c).getDemand();
        return ((DynamicCustomer) c).getDemand();
    }

    private CustomerType getType(int encodedId) {
        Object c = getCustomerObject(encodedId);
        if (c instanceof StaticCustomer) return ((StaticCustomer) c).getType();
        return ((DynamicCustomer) c).getType();
    }

    private double getStartTimeWindow(int encodedId) {
        Object c = getCustomerObject(encodedId);
        if (c instanceof StaticCustomer) return ((StaticCustomer) c).getStartTimeWindow();
        return ((DynamicCustomer) c).getStartTimeWindow();
    }

    private double getEndTimeWindow(int encodedId) {
        Object c = getCustomerObject(encodedId);
        if (c instanceof StaticCustomer) return ((StaticCustomer) c).getEndTimeWindow();
        return ((DynamicCustomer) c).getEndTimeWindow();
    }

    private double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }


}
