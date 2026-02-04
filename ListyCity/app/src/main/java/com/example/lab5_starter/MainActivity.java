package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private Button deleteCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;

    private FirebaseFirestore db;

    boolean isDeleteMode = false;

    private CollectionReference citiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");


        citiesRef.addSnapshotListener((value, error) -> {
            if(error != null){
                Log.e("Firestore", error.toString());
            }
            if(value != null && !value.isEmpty()) {
                cityArrayList.clear();
                for(QueryDocumentSnapshot snapshot: value){
                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");

                    cityArrayList.add(new City(name, province));
                }
                cityArrayAdapter.notifyDataSetChanged();
            }
        });

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        deleteCityButton = findViewById(R.id.deletebutton);
        cityListView = findViewById(R.id.listviewCities);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        addDummyData();

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(),"Add City");
        });

        deleteCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDeleteMode = true;
                deleteCityButton.setText("Tap a city to delete it"); // Visual feedback
            }
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {


            if (isDeleteMode) {

                City cityToRemove = cityArrayAdapter.getItem(i);
                cityArrayAdapter.remove(cityToRemove);
                isDeleteMode = false;
                deleteCityButton.setText("Delete City");
                //Gemini Feb 3rd 2026 , help me write code to delete data from firebase database android studio
                citiesRef.document(cityToRemove.getName())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("Firestore", "City successfully deleted!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("Firestore", "Error deleting document", e);
                                cityArrayAdapter.add(cityToRemove);
                                Toast.makeText(MainActivity.this, "Delete failed, check connection", Toast.LENGTH_SHORT).show();
                            }
                        });


                Toast.makeText(MainActivity.this, "Deleted city", Toast.LENGTH_SHORT).show();
            } else {
                City city = cityArrayAdapter.getItem(i);

                // Open the fragment
                CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
                cityDialogFragment.show(getSupportFragmentManager(),"City Details");
            }



        });

    }

    @Override
    public void updateCity(City city, String title, String year) {
        city.setName(title);
        city.setProvince(year);
        cityArrayAdapter.notifyDataSetChanged();

        // Updating the database using delete + addition
    }

    @Override
    public void addCity(City city){
        cityArrayList.add(city);
        cityArrayAdapter.notifyDataSetChanged();

        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.set(city);
    }

    public void addDummyData(){
        City m1 = new City("Edmonton", "AB");
        City m2 = new City("Vancouver", "BC");
        cityArrayList.add(m1);
        cityArrayList.add(m2);
        cityArrayAdapter.notifyDataSetChanged();
    }

    public void deleteCity(){
        DocumentReference docRef = db.collection("cities").document("BJ");
    }
}