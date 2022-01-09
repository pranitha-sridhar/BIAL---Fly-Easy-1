package com.fitnesshub.bial_flyeasy.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.fitnesshub.bial_flyeasy.R;
import com.fitnesshub.bial_flyeasy.adapters.ViewPagerAdapter;
import com.fitnesshub.bial_flyeasy.database.Prefs;
import com.fitnesshub.bial_flyeasy.databinding.ActivityMainBinding;
import com.fitnesshub.bial_flyeasy.databinding.LayoutGuidelinesBinding;
import com.fitnesshub.bial_flyeasy.databinding.LayoutProgressBinding;
import com.fitnesshub.bial_flyeasy.models.FlightModel;
import com.fitnesshub.bial_flyeasy.models.HomeModel;
import com.fitnesshub.bial_flyeasy.utils.Constants;
import com.fitnesshub.bial_flyeasy.utils.HelperClass;
import com.fitnesshub.bial_flyeasy.viewModels.MainViewModel;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
ActivityMainBinding activityBinding;
MainViewModel viewModel;
AlertDialog alertDialog;
List<String> guidelines;
Prefs prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs=new Prefs(this);
        viewModel= new ViewModelProvider(this).get(MainViewModel.class);
        activityBinding= DataBindingUtil.setContentView(MainActivity.this,R.layout.activity_main);
        setSupportActionBar(activityBinding.homeToolbar);
        activityBinding.drawerOpener.setOnClickListener(view -> activityBinding.drawerLayout.openDrawer(GravityCompat.START));
        activityBinding.navView.setNavigationItemSelectedListener(this);
        closeDrawer();
        activityBinding.weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,WeatherActivity.class);
                intent.putExtra("cityInt",prefs.getCityInt());
                startActivity(intent);
            }
        });
        activityBinding.orderFoodButton.setOnClickListener(view -> openFoodStoreActivity(true));
        activityBinding.shopButton.setOnClickListener(view -> openFoodStoreActivity(false));
        activityBinding.guidelinesButton.setOnClickListener(view -> guidelines());
        activityBinding.feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activityBinding.feedbackEditText.getText().toString().isEmpty()){
                    activityBinding.feedbackEditText.setError("Feedback cannot be empty");
                    activityBinding.feedbackEditText.requestFocus();
                }
                else {
                    HelperClass.toast(MainActivity.this,"Feedback submitted successfully");
                    activityBinding.feedbackEditText.setText("");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (activityBinding.drawerLayout.isDrawerOpen(GravityCompat.START))
            activityBinding.drawerLayout.closeDrawer(GravityCompat.START);
        else {
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setTitle("EXIT");
            alert.setMessage("Do you want to exit the application?");
            alert.setCancelable(false);
            alert.setPositiveButton("Exit", (dialog, which) -> {
                Intent a = new Intent(Intent.ACTION_MAIN);
                a.addCategory(Intent.CATEGORY_HOME);
                a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(a);
            });
            alert.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = alert.create();
            dialog.show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_home:
                closeDrawer();
                break;
            case R.id.nav_profile:
                startActivity(new Intent(this,ProfileActivity.class));
                break;
            case R.id.nav_bookFlight:
                //startActivity(new Intent(this,ProfileActivity.class));
                break;
            case R.id.nav_latestBookings:
                //startActivity(new Intent(this,ProfileActivity.class));
                break;
            case R.id.nav_maps:
                //startActivity(new Intent(this,ProfileActivity.class));
                break;
            case R.id.nav_food:
                HelperClass.toast(this,"clicked on food");
                openFoodStoreActivity(true);
                break;
            case R.id.nav_cab:
                //startActivity(new Intent(this,ProfileActivity.class));
                break;
            case R.id.nav_shops:
                openFoodStoreActivity(false);
                break;
            case R.id.nav_logout:
                //startActivity(new Intent(this,ProfileActivity.class));
                break;    
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        viewModel.getData("61c9526061c3930022ea8fdf","BIAL");
        viewModel.getResponse().observe(this,responseResource->{
            if (responseResource == null) {HelperClass.toast(this,"No response");}
            if (responseResource.status == Constants.IN_PROGRESS) {
                activityBinding.setStatus(Constants.IN_PROGRESS);
            }
            else if (responseResource.status == Constants.OKAY) {
                activityBinding.setCityName(Constants.airportNames[prefs.getCityInt()]);
                activityBinding.setStatus(Constants.OKAY);
                activityBinding.setHomeModel(responseResource);
                tabLayout(responseResource.arrival_flights,responseResource.departure_flights);
                guidelines=responseResource.guidelines;
            } else {
                HelperClass.toast(this, " "+responseResource.status);
                //finish();
            }
        });
        viewModel.displayToastMsg().observe(this,messge->{HelperClass.toast(this,messge);});
    }

    private void closeDrawer() { activityBinding.drawerLayout.closeDrawer(GravityCompat.START); }

    public void guidelines(){
        LayoutGuidelinesBinding layoutBinding;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        layoutBinding = LayoutGuidelinesBinding.inflate(LayoutInflater.from(this));
        builder.setView(layoutBinding.getRoot());
        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
        alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        alertDialog.getWindow().setBackgroundDrawable(null);
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);

        StringBuilder stringBuilder = new StringBuilder();
        for (String s: guidelines) {
            stringBuilder.append(s);
            stringBuilder.append("\n\n");
        }
        layoutBinding.allGuidelines.setText(stringBuilder.toString().trim());
        layoutBinding.dismissButton.setOnClickListener(view -> alertDialog.dismiss());
    }

    private void tabLayout(List<FlightModel> arrival, List<FlightModel> departure){
        activityBinding.tabLayout.addTab(activityBinding.tabLayout.newTab().setText("Tab 1"));
        activityBinding.tabLayout.addTab(activityBinding.tabLayout.newTab().setText("Tab 2"));
        activityBinding.tabLayout.setupWithViewPager(activityBinding.viewPager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(),activityBinding.tabLayout.getTabCount(), this,arrival,departure);
        activityBinding.viewPager.setAdapter(adapter);
    }
    
    public void openFoodStoreActivity(boolean isFood){
        Intent intent=new Intent(MainActivity.this,FoodStoresActivity.class);
        intent.putExtra("isFood",isFood);
        startActivity(intent);
    }
}