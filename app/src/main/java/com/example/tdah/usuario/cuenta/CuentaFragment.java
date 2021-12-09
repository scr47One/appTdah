package com.example.tdah.usuario.cuenta;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tdah.R;
import com.example.tdah.UsuarioPrincipal;
import com.example.tdah.modelos.UsuarioPadreTutor;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.regex.Pattern;

public class CuentaFragment extends Fragment {

    private CuentaViewModel cuentaViewModel;
    private FirebaseAuth mAuth;
    private FirebaseUser fUser;
    private DatabaseReference databaseReference;
    private Button btn_editar;
    private Button btn_guardar;
    private EditText txt_nombre;
    private EditText txt_apellido_paterno;
    private EditText txt_apellido_materno;
    private EditText txt_contrasena_actual;
    private EditText txt_contrasena_nueva;
    private EditText txt_correo;
    private boolean boolean_correo;
    private boolean boolean_contrasena;

    public CuentaFragment() {
        super(R.layout.fragment_cuenta);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
         fUser = FirebaseAuth.getInstance().getCurrentUser();

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cuentaViewModel =
                new ViewModelProvider(this).get(CuentaViewModel.class);
        View root = inflater.inflate(R.layout.fragment_cuenta, container, false);


        txt_nombre = root.findViewById(R.id.txt_cuenta_nombre);
        txt_apellido_paterno = root.findViewById(R.id.txt_cuenta_apellido_paterno);
        txt_apellido_materno = root.findViewById(R.id.txt_cuenta_apellido_materno);

        txt_contrasena_actual = root.findViewById(R.id.txt_cuenta_contrasenia_actual);
        txt_contrasena_nueva = root.findViewById(R.id.txt_cuenta_contrasenia_nueva);

        txt_correo = root.findViewById(R.id.txt_cuenta_correo);
        btn_editar = root.findViewById(R.id.btn_cuenta_editar);
        btn_guardar = root.findViewById(R.id.btn_cuenta_guardar);

        mAuth = FirebaseAuth.getInstance();
        fUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        txt_contrasena_actual.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                valida_contrasena(txt_contrasena_actual);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        txt_contrasena_nueva.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                valida_contrasena(txt_contrasena_nueva);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        txt_correo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                valida_correo(txt_correo);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        btn_editar.setOnClickListener(v -> {
            txt_contrasena_actual.setEnabled(true);
            valida_contrasena(txt_contrasena_actual);
            txt_contrasena_nueva.setEnabled(true);
            txt_correo.setEnabled(true);
        });



        btn_guardar.setOnClickListener(v -> {

            if (!boolean_correo&&!txt_correo.getText().toString().equals(fUser.getEmail()))
                actualizaCorreo(txt_correo.getText().toString(),txt_contrasena_actual.getText().toString());

            if (!boolean_contrasena)
                actualizaContrasena(txt_contrasena_actual.getText().toString(),txt_contrasena_nueva.getText().toString());

        });

        datosUsuario();

        return root;
    }

    private void datosUsuario() {
        UsuarioPadreTutor u = new UsuarioPadreTutor();

        u.setString_id(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
        u.setString_correo(fUser.getEmail());

        databaseReference.child("Usuario").child(u.getString_id()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    u.setString_nombre(Objects.requireNonNull(snapshot.child("string_nombre").getValue()).toString());
                    u.setString_apellido_paterno(Objects.requireNonNull(snapshot.child("string_apellido_paterno").getValue()).toString());
                    u.setString_apellido_materno(Objects.requireNonNull(snapshot.child("string_apellido_materno").getValue()).toString());
                    u.setString_curp(Objects.requireNonNull(snapshot.child("string_curp").getValue()).toString());
                    txt_nombre.setText(u.getString_nombre());
                    txt_apellido_paterno.setText(u.getString_apellido_paterno());
                    txt_apellido_materno.setText(u.getString_apellido_materno());
                    txt_correo.setText(u.getString_correo());
                    Log.e(TAG, "Datos recuperados");

                } else {
                    Log.e(TAG, "Datos no recuperados");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Log.w(TAG, "loadPost:onCancelled", error.toException());
            }
        });

    }

    private void valida_contrasena(EditText editText_contrasena) {

        editText_contrasena.setError(null);

        String Password = editText_contrasena.getText().toString().trim();

        boolean_contrasena = false;

        View focusView = null;

        if (TextUtils.isEmpty(Password)) {
            editText_contrasena.setError(getString(R.string.error_campo_requerido));
            focusView = editText_contrasena;
            boolean_contrasena = true;
        }

        if (!Password.matches(".*[!@#$%^&*+=?-].*")) {
            editText_contrasena.setError(getString(R.string.error_caracter_especial_requerido));
            focusView = editText_contrasena;
            boolean_contrasena = true;
        }

        if (!Password.matches(".*\\d.*")) {
            editText_contrasena.setError(getString(R.string.error_numero_requerido));
            focusView = editText_contrasena;
            boolean_contrasena = true;
        }

        if (!Password.matches(".*[a-z].*")) {
            editText_contrasena.setError(getString(R.string.error_no_se_encontraron_minusculas));
            focusView = editText_contrasena;
            boolean_contrasena = true;
        }

        if (!Password.matches(".*[A-Z].*")) {
            editText_contrasena.setError(getString(R.string.error_no_se_encontraron_mayusculas));
            focusView = editText_contrasena;
            boolean_contrasena = true;
        }

        if (!Password.matches(".{8,15}")) {
            editText_contrasena.setError(getString(R.string.error_contrasena_muy_corta));
            focusView = editText_contrasena;
            boolean_contrasena = true;
        }

        if (Password.matches(".*\\s.*")) {
            editText_contrasena.setError(getString(R.string.error_sin_espacios));
            focusView = editText_contrasena;
            boolean_contrasena = true;
        }

        if (boolean_contrasena) {

            focusView.requestFocus();
            btn_guardar.setEnabled(false);

        }else{
            btn_guardar.setEnabled(true);
        }

    }


    private void valida_correo(EditText editText_correo) {

        editText_correo.setError(null);

         boolean_correo = false;

        View focusView = null;

        Pattern pattern = Patterns.EMAIL_ADDRESS;

        String Email = editText_correo.getText().toString().trim();

        if (TextUtils.isEmpty(Email)) {
            editText_correo.setError(getString(R.string.error_campo_requerido));
            focusView = editText_correo;
            boolean_correo = true;
        }
        if (!pattern.matcher(Email).matches()) {
            editText_correo.setError(getString(R.string.error_correo_no_valido));
            focusView = editText_correo;
            boolean_correo = true;
        }
        if (boolean_correo) {

            focusView.requestFocus();
            btn_guardar.setEnabled(false);

        }else{
            btn_guardar.setEnabled(true);
        }
    }

    public void actualizaCorreo(String string_correo,String string_contrasena) {
        Toast.makeText(getContext(), "Actualizando correo", Toast.LENGTH_SHORT).show();
        AuthCredential credential = EmailAuthProvider.getCredential(fUser.getEmail(),string_contrasena);
        fUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                fUser.updateEmail(string_correo)
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                databaseReference.child("Usuario").child(fUser.getUid()).child("string_correo").setValue(string_correo);
                                Toast.makeText(getContext(), "El correo se actualizó con éxito", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(getContext(), UsuarioPrincipal.class));

                            }else{
                                Toast.makeText(getContext(), "ERROR: No se actualizó el correo"+task1.getResult().toString(), Toast.LENGTH_LONG).show();
                            }
                        });
            }else{
                Toast.makeText(getContext(), "ERROR: Contraseña actual incorrecta",Toast.LENGTH_LONG).show();
            }
        });

    }

    public void actualizaContrasena(String string_contrasena_actual,String string_contrasena_nueva) {

        Toast.makeText(getContext(), "Actualizando contraseña", Toast.LENGTH_SHORT).show();
        AuthCredential credential = EmailAuthProvider.getCredential(fUser.getEmail(),string_contrasena_actual);
        fUser.reauthenticate(credential).addOnCompleteListener(task1 -> {
            if(task1.isSuccessful()){
                fUser.updatePassword(string_contrasena_nueva)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "La contraseña se actualizó con éxito", Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(getContext(), "ERROR: No se actualizó la contraseña"+task.getResult().toString(), Toast.LENGTH_LONG).show();
                            }
                        });
            }else{
                Toast.makeText(getContext(), "ERROR: Contraseña actual incorrecta",Toast.LENGTH_LONG).show();
            }

        });

    }
}