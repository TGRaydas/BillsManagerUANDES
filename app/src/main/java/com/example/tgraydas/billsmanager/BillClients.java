package com.example.tgraydas.billsmanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BillClients extends AppCompatActivity {
    private static final String TAG = "Y EL NUMERO ES.....!!!";
    NetworkManager networkManager;
    Desk desk;
    ProductsGridAdapter productsGridAdapter;
    ProductsSpinnerAdapter productsSpinnerAdapter;
    ArrayList<Product> billProductList = new ArrayList<>();
    ArrayList<Product> productList = new ArrayList<>();
    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_clients);
        networkManager = NetworkManager.getInstance(this);
        GridView gridView = findViewById(R.id.gridViewProducts);
        final Spinner spinner = findViewById(R.id.spinner);
        getProducts(productList, gridView, spinner);
        productsGridAdapter = new ProductsGridAdapter(this, billProductList);
        gridView.setAdapter(productsGridAdapter);
        Button button = findViewById(R.id.Add_product_to_bill);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProductToBill(spinner, productsGridAdapter);
            }
        });

        int extras = getIntent().getIntExtra("Desk", 0);
        Log.d(TAG, "onCreate: " + extras);
        TextView tablesBill = (TextView) findViewById(R.id.table_name_bills_client_tx);
        tablesBill.setText(String.format("Mesa %d", extras));
    }

    public void getProducts(final ArrayList<Product> productList, final GridView gridView, final Spinner spinner) {
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
                        Product product = new Product(id, price, name, detail);
                        productList.add(product);
                    }
                    productsSpinnerAdapter = new ProductsSpinnerAdapter(getApplicationContext(), R.id.spinner ,productList);
                    spinner.setAdapter(productsSpinnerAdapter);


                } catch (JSONException e) {

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.getMessage());
            }
        });
    }

    public void addProductToBill(Spinner spinner, ProductsGridAdapter productsGridAdapter){
        billProductList.add((Product) spinner.getSelectedItem());
        productsGridAdapter.notifyDataSetChanged();

    }

    public void removeProductFromBill(int position){
        billProductList.remove(position);
        productsGridAdapter.notifyDataSetChanged();
    }


}
