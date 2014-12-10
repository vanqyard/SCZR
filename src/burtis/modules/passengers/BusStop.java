package burtis.modules.passengers;

import burtis.common.events.Simulation.BusStopsListEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Random;
import java.util.logging.Level;

/**
 * Representation of bus stop in passengers module.
 * 
 * @author Mikołaj Sowiński
 */
public class BusStop {
   
    private final int id;
    private final String name;
    
    private final Queue<Passenger> passengerQueue = new LinkedList<>();
    private final Queue<Bus> busQueue = new LinkedList<>();
    
    private static final Random randomGenerator = new Random();
    
    private Bus busAtBusStop;
    
    private final static List<BusStop> busStops = new ArrayList<>();

    private BusStop(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public static void add(List<BusStopsListEvent.BusStop> busStopsList) {
        for(BusStopsListEvent.BusStop busStop: busStopsList) {
            busStops.add(new BusStop(busStop.busStopId, busStop.busStopName));
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "BusStop{" + "id=" + id + ", name=" + name + '}';
    }
    
    public void enqueueBus(Bus bus) {
        busQueue.add(bus);
        PassengerModule.getInstance().getLogger().log(Level.INFO, "{0} arrived at {1}", new Object[]{bus, this});
    }
    
    public void enqueuePassenger(Passenger passenger) {
        passengerQueue.add(passenger);
        PassengerModule.getInstance().getLogger().log(Level.INFO, "{0} generated at {1}", new Object[]{passenger, this});
    }
    
    public void departBus() {
        busAtBusStop = null;
    }
    
    public Bus getCurrentBus() {
        return busAtBusStop;
    }
    
    public void nextBus() {
        busAtBusStop = busQueue.poll();
        if(busAtBusStop != null) {
            Transaction.newTransaction(busAtBusStop, this);
        }
    }
    
    public Passenger getNextPassenger() {
        return passengerQueue.poll();
    }
    
    public static BusStop getBusStop(int id) {
        for(BusStop busStop : busStops) {
            if(busStop.getId() == id) return busStop;
        }
        
        return null;
    }
    
    /**
     * Returns random bus stop excluding last one.
     * 
     * @return random bus stop
     */
    public static BusStop getRandomBusStop() {
        int busStopIndex = randomGenerator.nextInt(busStops.size()-1);
        return busStops.get(busStopIndex);
    }
    
    /**
     * Returns random destination bus stop from range of next from origin to the
     * last in the line.
     * 
     * @param origin origin bus stop
     * @return destination bus stop
     */
    public static BusStop getRandomNextBusStop(BusStop origin) {
        int originIndex = busStops.indexOf(origin);
        int nextBusStopIndex = randomGenerator.nextInt(busStops.size()-1-originIndex);
        nextBusStopIndex+=originIndex;
        return busStops.get(nextBusStopIndex);
    }
    
    /**
     * Returns number of passengers waiting at the bus stop of given id.
     * 
     * @param busStopId bus stop id
     * @return number of waiting passengers
     */
    public static int waitingPassengers(int busStopId) {
        BusStop busStop = getBusStop(busStopId);
        return busStop.passengerQueue.size();
    }

}
