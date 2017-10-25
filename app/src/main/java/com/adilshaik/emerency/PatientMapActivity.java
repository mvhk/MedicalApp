package com.adilshaik.emerency;

        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.location.Location;
        import android.location.LocationListener;
        import android.support.annotation.NonNull;
        import android.support.annotation.Nullable;
        import android.support.v4.app.ActivityCompat;
        import android.support.v4.app.FragmentActivity;
        import android.os.Bundle;
        import android.support.v7.app.AlertDialog;
        import android.util.Log;
        import android.view.MotionEvent;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.TextView;
        import android.widget.Toast;
        import com.bumptech.glide.Glide;
        import com.firebase.geofire.GeoFire;
        import com.firebase.geofire.GeoLocation;
        import com.firebase.geofire.GeoQuery;
        import com.firebase.geofire.GeoQueryEventListener;
        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
        import com.google.android.gms.common.GooglePlayServicesRepairableException;
        import com.google.android.gms.common.api.GoogleApiClient;
        import com.google.android.gms.location.LocationRequest;
        import com.google.android.gms.common.api.Status;
        import com.google.android.gms.location.places.Place;
        import com.google.android.gms.location.places.ui.PlaceAutocomplete;
        import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
        import com.google.android.gms.location.places.ui.PlaceSelectionListener;
        import com.google.android.gms.location.LocationServices;
        import com.google.android.gms.vision.text.Line;
        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.OnMapReadyCallback;
        import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.BitmapDescriptorFactory;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.Marker;
        import com.google.android.gms.maps.model.MarkerOptions;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.database.ChildEventListener;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.ValueEventListener;

        import java.text.ParseException;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;

public class PatientMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private Button mLogout, mRequest, mSettings;

    private LatLng pickupLocation;

    private Boolean requestBol = false;

    private Marker pickupMarker;


    private SupportMapFragment mapFragment;



    private LinearLayout mDoctorInfo;

    private ImageView mDoctorProfileImage;
    private TextView mDoctorName, mDoctorPhone, mDoctorDid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(PatientMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                    }else{
                        mapFragment.getMapAsync(this);
                    }

                mDoctorInfo = (LinearLayout) findViewById(R.id.doctorInfo);

                        mDoctorProfileImage = (ImageView) findViewById(R.id.doctorProfileImage);

                        mDoctorName = (TextView) findViewById(R.id.doctorName);
                mDoctorPhone = (TextView) findViewById(R.id.doctorPhone);
                mDoctorDid = (TextView) findViewById(R.id.doctorDid);




        mLogout = (Button) findViewById(R.id.logout);
        mRequest = (Button) findViewById(R.id.request);
        mSettings = (Button) findViewById(R.id.settings);



        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(PatientMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (requestBol){
                    requestBol = false;
                    geoQuery.removeAllListeners();
                    doctorLocationRef.removeEventListener(doctorLocationRefListener);


                    if (doctorFoundID != null){
                        DatabaseReference doctorRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Doctors").child(doctorFoundID).child("patientTreatId");
                        doctorRef.removeValue();
                        doctorFoundID = null;

                    }
                    doctorFound = false;
                    radius = 1;
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("patientRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(userId);

                    if(pickupMarker != null){
                        pickupMarker.remove();
                    }
                    if (mDoctorMarker != null){
                        mDoctorMarker.remove();
                    }
                    mRequest.setText("call Emergency");

                    mDoctorInfo.setVisibility(View.GONE);
                    mDoctorName.setText("");
                    mDoctorPhone.setText("");
                    mDoctorDid.setText("Cause: --");
                    mDoctorProfileImage.setImageResource(R.mipmap.ic_default_user);
                }else{
                    requestBol = true;

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("patientRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                    pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Emergency Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_patient)));

                    mRequest.setText("Getting your Doctor....");

                    getClosestDoctor();
                }
            }
        });
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PatientMapActivity.this, PatientSettingsActivity.class);
                startActivity(intent);
                return;
            }
        });
    }
    private int radius = 1;
    private Boolean doctorFound = false;
    private String doctorFoundID;

    GeoQuery geoQuery;
    private void getClosestDoctor(){
        DatabaseReference doctorLocation = FirebaseDatabase.getInstance().getReference().child("doctorsAvailable");

        GeoFire geoFire = new GeoFire(doctorLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!doctorFound && requestBol){
                    doctorFound = true;
                    doctorFoundID = key;

                    DatabaseReference doctorRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Doctors").child(doctorFoundID).child("patientRequest");
                    String patientId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("patientTreatId", patientId);
                    doctorRef.updateChildren(map);

                    getDoctorLocation();
                    getDoctorInfo();
                    mRequest.setText("Looking for Doctor's Location....");

                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!doctorFound)
                {
                    radius++;
                    getClosestDoctor();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    private Marker mDoctorMarker;
    private DatabaseReference doctorLocationRef;
    private ValueEventListener doctorLocationRefListener;
    private void getDoctorLocation(){

        doctorLocationRef = FirebaseDatabase.getInstance().getReference().child("doctorsWorking").child(doctorFoundID).child("l");
        doctorLocationRefListener = doctorLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestBol){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng doctorLatLng = new LatLng(locationLat,locationLng);
                    if(mDoctorMarker != null){
                        mDoctorMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(doctorLatLng.latitude);
                    loc2.setLongitude(doctorLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance<100){
                        mRequest.setText("Doctor Arrived");
                    }else{
                        mRequest.setText("Doctor Found: " + String.valueOf(distance));
                    }



                    mDoctorMarker = mMap.addMarker(new MarkerOptions().position(doctorLatLng).title("your doctor").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_doctor)));

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }


    private void getDoctorInfo(){
        mDoctorInfo.setVisibility(View.VISIBLE);
        DatabaseReference mPatientDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Doctors").child(doctorFoundID);
        mPatientDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                  if(map.get("name")!=null){
                      mDoctorName.setText(map.get("name").toString());
                                        }
                  if(map.get("phone")!=null){
                      mDoctorPhone.setText(map.get("phone").toString());
                                        }
                  if(map.get("did")!=null){
                      mDoctorDid.setText(map.get("Did").toString());
                                      }
                  if(map.get("profileImageUrl")!=null){
                      Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(mDoctorProfileImage);
                  }

              }
          }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                                    }
        });
           }







    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(PatientMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext()!=null){
            mLastLocation = location;

            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    final int LOCATION_REQUEST_CODE = 1;
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
                switch (requestCode) {
                        case LOCATION_REQUEST_CODE: {
                                // If request is cancelled, the result arrays are empty.
                                        if (grantResults.length > 0
                                               && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                                        mapFragment.getMapAsync(this);
                                        } else {
                                          Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                                        }
                            break;
                        }
                }



    }
}
