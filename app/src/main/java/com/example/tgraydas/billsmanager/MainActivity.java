package com.example.tgraydas.billsmanager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.master.glideimageview.GlideImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity
implements NavigationView.OnNavigationItemSelectedListener,
        AllTablesFragment.takeTable,
        MyTablesFragment.takeMyTable
{

    private NetworkManager networkManager;
    private SharedPreferences sharedPreferences;
    private TextView navHead;
    GlideImageView glideImageViewDishOfDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        networkManager = NetworkManager.getInstance(this);
        ArrayList<Product> productList = new ArrayList<>();
        ArrayList<Desk> deskList = new ArrayList<>();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View fragmentContainer = findViewById(R.id.fragment_container);
        if (fragmentContainer != null) {
            if (savedInstanceState != null) {
                return;
            }
        }


        /*  LOGIN */
        sharedPreferences = getApplicationContext().getSharedPreferences(
                getString(R.string.loginpreferences), Context.MODE_PRIVATE);

        String token = sharedPreferences.getString("token", "");


        if (Objects.equals(token, "")) {
            /* descomentar cuando este listo el login */
            Intent goToLoginIntent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(goToLoginIntent);
            MainActivity.this.finish();
            Toast initialized_message =
                    Toast.makeText(getApplicationContext(),
                            "Debes Iniciar Sesion!" + ("\ud83d\ude01"), Toast.LENGTH_SHORT);

            initialized_message.show();
        } else {
            getDishOfDay(productList);
            networkManager.setToken(token);
        }

        setTitle("Bills Manager");

    }


    public void killBill(Bill bill){

    }

    public void login(){
        try{
            networkManager.login(new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        }catch (JSONException e){

        }
    }

    public void getDishOfDay(final ArrayList<Product> productList) {
        networkManager.getProducts(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray data = response.optJSONArray("0");
                    for (int i = 0; i < data.length(); i++){
                        int id = data.getJSONObject(i).optInt("id");
                        String name = data.getJSONObject(i).optString("name");
                        int price = data.getJSONObject(i).optInt("price");
                        String detail = data.getJSONObject(i).optString("detail");
                        URL url;
                        try {
                            url = new URL(new URL(networkManager.BASE_URL), data.getJSONObject(i).optString("img_url"));
                            Product product = new Product(id, price, name, detail, url);
                            productList.add(product);
                        }catch (MalformedURLException e){

                        }
                    }

                } catch (JSONException e) {

                }
                int random = (int) (Math.random() * productList.size());
                Product dishOfDay = (Product) productList.get(random);

                TextView dishOfDayInfo = (TextView) findViewById(R.id.dishofday_information_textview);
                dishOfDayInfo.setText("Nombre: \n" + dishOfDay.name + "\n" + "Precio: $" + dishOfDay.price);
                /* Dish of Day */
                glideImageViewDishOfDay = (GlideImageView)findViewById(R.id.glide_image_content_main);
                glideImageViewDishOfDay.loadImageUrl(dishOfDay.url.toString());

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {

        int count = getFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
            //additional code
        } else {
            getFragmentManager().popBackStack();
        }

    }

    public void getMyDesks(final ArrayList<Desk> deskList, final MyTablesFragment myTablesFragment, final Context context){
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        networkManager.getMyDesks(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONArray data = response.optJSONArray("0");
                for (int i = 0; i < data.length(); i++) {
                    try {
                        int id = data.getJSONObject(i).optInt("id");
                        int number = data.getJSONObject(i).optInt("number");
                        Desk desk = new Desk(id, number);
                        deskList.add(desk);
                    } catch (JSONException e) {

                    }
                }
                myTablesFragment.populateMyTables(deskList);
                progress.dismiss();
                if (deskList.size() == 0){
                    Toast.makeText(context, "No tienes mesas", Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
                Toast.makeText(context, "No tienes mesas", Toast.LENGTH_LONG).show();
            }
        });
    }


    public void getDesks(final ArrayList<Desk> deskList, final AllTablesFragment fragment){
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        networkManager.getDesks(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONArray data = response.optJSONArray("0");
                for (int i = 0; i < data.length(); i++) {
                    try {
                        int id = data.getJSONObject(i).optInt("id");
                        int number = data.getJSONObject(i).optInt("number");
                        Desk desk = new Desk(id, number);
                        deskList.add(desk);
                    } catch (JSONException e) {

                    }
                }
                fragment.populateAllTables(deskList);
                progress.dismiss();


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(findViewById(R.id.fragment_container) != null){
            if (id == R.id.nav_all_tables) {
                AllTablesFragment allTablesFragment = new AllTablesFragment();
                allTablesFragment.setArguments(getIntent().getExtras());
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.fragment_container, allTablesFragment, allTablesFragment.toString());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                ArrayList<Desk> tables = new ArrayList<>();
                getDesks(tables, allTablesFragment);
            } else if (id == R.id.nav_waiter_tables) {
                MyTablesFragment myTablesFragment = new MyTablesFragment();
                myTablesFragment.setArguments(getIntent().getExtras());
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.fragment_container, myTablesFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                ArrayList<Desk> tables = new ArrayList<>();
                getMyDesks(tables, myTablesFragment, this);
            } else if (id == R.id.nav_logout) {
                    /* LOGOUT */
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("token");
                editor.commit();
                Intent goToLoginIntent = new Intent(MainActivity.this, LoginActivity.class);
                MainActivity.this.startActivity(goToLoginIntent);
                finish();
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void takeTableListener(Desk desk) {
        Intent goToOrderWithTable = new Intent(getApplicationContext(), BillClients.class);
        goToOrderWithTable.putExtra("Desk", desk.id);
        startActivity(goToOrderWithTable);
    }

    @Override
    public void takeMyTableListener(Desk desk) {
        Intent goToOrderWithTable = new Intent(getApplicationContext(), BillClients.class);
        goToOrderWithTable.putExtra("Desk", desk.id);
        startActivity(goToOrderWithTable);
    }
}
