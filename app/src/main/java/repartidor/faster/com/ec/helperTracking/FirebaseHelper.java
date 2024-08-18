package repartidor.faster.com.ec.helperTracking;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import repartidor.faster.com.ec.model.Driver;

public class FirebaseHelper {

    private static final String ONLINE_DRIVERS = "online_drivers";

    private final DatabaseReference onlineDriverDatabaseReference;

    public FirebaseHelper(String driverId) {
        onlineDriverDatabaseReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(ONLINE_DRIVERS)
                .child(driverId);
                //onlineDriverDatabaseReference
                //.onDisconnect()
                //.removeValue();
    }

    public void updateDriver(Driver driver) {
        onlineDriverDatabaseReference
                .setValue(driver);
    }

    public void deleteDriver() {
        onlineDriverDatabaseReference
                .removeValue();
    }

}
