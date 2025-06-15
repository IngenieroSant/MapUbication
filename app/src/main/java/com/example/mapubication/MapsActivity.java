package com.example.mapubication;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    private EditText inputBusqueda;
    private Button btnBuscar, btnLimpiarBuscados, btnEliminarIniciales, btnMenu;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private final List<Marker> marcadoresBuscados = new ArrayList<>();
    private final List<Marker> marcadoresIniciales = new ArrayList<>();
    private final LinkedHashSet<String> historialBusquedas = new LinkedHashSet<>();
    private String ultimaBusqueda = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Inicializar vistas
        inputBusqueda = findViewById(R.id.inputBusqueda);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnLimpiarBuscados = findViewById(R.id.btnLimpiarBuscados);
        btnEliminarIniciales = findViewById(R.id.btnEliminarIniciales);
        btnMenu = findViewById(R.id.btnMenu);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);

        // Configurar mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Configurar listeners
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        btnBuscar.setOnClickListener(v -> {
            String lugar = inputBusqueda.getText().toString().trim();
            if (!lugar.isEmpty()) {
                buscarLugar(lugar, false); // false = búsqueda nueva
            } else {
                Toast.makeText(this, "Ingresa un lugar para buscar", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón modificado para limpiar el campo de búsqueda
        btnLimpiarBuscados.setOnClickListener(v -> {
            inputBusqueda.setText(""); // Limpiar el campo de texto
            inputBusqueda.requestFocus(); // Opcional: mantener el foco en el campo
            Toast.makeText(this, "Campo de búsqueda limpiado", Toast.LENGTH_SHORT).show();
        });

        btnEliminarIniciales.setOnClickListener(v -> {
            eliminarTodosLosMarcadores();
        });
    }

    private void eliminarTodosLosMarcadores() {
        // Eliminar marcadores iniciales
        for (Marker m : marcadoresIniciales) {
            m.remove();
        }
        marcadoresIniciales.clear();

        // Eliminar marcadores buscados
        for (Marker m : marcadoresBuscados) {
            m.remove();
        }
        marcadoresBuscados.clear();

        Toast.makeText(this, "Todos los marcadores eliminados", Toast.LENGTH_SHORT).show();
    }

    private void buscarLugar(String lugar, boolean desdeHistorial) {
        ultimaBusqueda = lugar; // Actualizar siempre la última búsqueda

        if (!desdeHistorial) {
            // Solo agregar al historial si no viene de selección del historial
            if (historialBusquedas.contains(lugar)) {
                historialBusquedas.remove(lugar);
            }
            historialBusquedas.add(lugar);

            // Mantener máximo 10 búsquedas
            if (historialBusquedas.size() > 10) {
                String oldest = historialBusquedas.iterator().next();
                historialBusquedas.remove(oldest);
            }
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> direcciones = geocoder.getFromLocationName(lugar, 1);
            if (direcciones != null && !direcciones.isEmpty()) {
                Address direccion = direcciones.get(0);
                LatLng posicion = new LatLng(direccion.getLatitude(), direccion.getLongitude());

                // Limpiar marcadores previos
                for (Marker m : marcadoresBuscados) {
                    m.remove();
                }
                marcadoresBuscados.clear();

                // Agregar nuevo marcador
                Marker marcador = mMap.addMarker(new MarkerOptions()
                        .position(posicion)
                        .title(lugar));
                marcadoresBuscados.add(marcador);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 12f));

                // Actualizar historial en UI
                mostrarHistorial();
            } else {
                Toast.makeText(this, "Ubicación no encontrada", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("MAPS", "Error en geocoder", e);
            Toast.makeText(this, "Error al buscar la ubicación", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarHistorial() {
        navigationView.getMenu().removeGroup(R.id.group_historial);

        MenuItem limpiarItem = navigationView.getMenu().findItem(R.id.nav_limpiar);
        limpiarItem.setVisible(!historialBusquedas.isEmpty());

        if (!historialBusquedas.isEmpty()) {
            // Agregar título
            MenuItem titulo = navigationView.getMenu()
                    .add(R.id.group_historial, Menu.NONE, Menu.NONE, "Recientes");
            titulo.setEnabled(false);

            // Estilo del título
            SpannableString spannableString = new SpannableString(titulo.getTitle());
            spannableString.setSpan(new ForegroundColorSpan(Color.GRAY), 0, spannableString.length(), 0);
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableString.length(), 0);
            titulo.setTitle(spannableString);

            // Mostrar en orden inverso (más reciente primero)
            List<String> busquedasOrdenadas = new ArrayList<>(historialBusquedas);
            Collections.reverse(busquedasOrdenadas);

            for (String busqueda : busquedasOrdenadas) {
                MenuItem item = navigationView.getMenu()
                        .add(R.id.group_historial, Menu.NONE, Menu.NONE, busqueda);

                // Mostrar icono solo en la última búsqueda
                if (busqueda.equals(ultimaBusqueda)) {
                    item.setIcon(R.drawable.ic_history);
                }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_volver) {
            volverAMainActivity();

        } else if (id == R.id.nav_limpiar) {
            Log.d("HISTORIAL", "Antes de limpiar: " + historialBusquedas);
            limpiarHistorialCompleto();
            Log.d("HISTORIAL", "Después de limpiar: " + historialBusquedas);

        } else if (!item.getTitle().toString().equals("Recientes")) {
            String busqueda = item.getTitle().toString();
            inputBusqueda.setText(busqueda);
            buscarLugar(busqueda, true);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void limpiarHistorialCompleto() {
        historialBusquedas.clear();
        ultimaBusqueda = "";

        mostrarHistorial();
        drawerLayout.closeDrawer(GravityCompat.START);

        Toast.makeText(this, "Historial borrado completamente", Toast.LENGTH_SHORT).show();
    }

    private void volverAMainActivity() {
        startActivity(new Intent(this, com.example.mapubication.MainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Marcadores iniciales
        Marker m1 = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(10.3910, -75.4794))
                .title("Cartagena"));
        Marker m2 = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(10.9878, -74.7889))
                .title("Barranquilla"));
        Marker m3 = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(11.2408, -74.1990))
                .title("Santa Marta"));

        marcadoresIniciales.add(m1);
        marcadoresIniciales.add(m2);
        marcadoresIniciales.add(m3);

        // Centrar mapa en Colombia
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(4.5709, -74.2973), 5.5f));
        mostrarHistorial();
    }
}