package com.example.mislugares;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LugarAdapter extends RecyclerView.Adapter<LugarAdapter.LugarViewHolder> {
    private List<Lugar> listaLugares;

    public LugarAdapter(List<Lugar> listaLugares) {
        this.listaLugares = listaLugares;
    }

    @NonNull
    @Override
    public LugarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lugar, parent, false);
        return new LugarViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull LugarViewHolder holder, int position) {
        Lugar lugar = listaLugares.get(position);
        holder.textNombre.setText(lugar.getNombre());
        holder.textDireccion.setText(lugar.getDireccion());
        holder.ratingBar.setRating(lugar.getValoracion());

        // Cargar ícono en base al tipo
        switch (lugar.getTipo()) {
            case "Bar":
                holder.icono.setImageResource(R.drawable.tipo_icon_bar);
                break;
            case "Compras":
                holder.icono.setImageResource(R.drawable.tipo_icon_compras);
                break;
            case "Cafe":
                holder.icono.setImageResource(R.drawable.tipo_icon_cup);
                break;
            case "Deporte":
                holder.icono.setImageResource(R.drawable.tipo_icon_deporte);
                break;
            case "Educacion":
                holder.icono.setImageResource(R.drawable.tipo_icon_educacion);
                break;
            case "Espectaculo":
                holder.icono.setImageResource(R.drawable.tipo_icon_espectaculo);
                break;
            case "Gasolineria":
                holder.icono.setImageResource(R.drawable.tipo_icon_gas);
                break;
            case "Hotel":
                holder.icono.setImageResource(R.drawable.tipo_icon_hotel);
                break;
            case "Naturaleza":
                holder.icono.setImageResource(R.drawable.tipo_icon_naturaleza);
                break;
            case "Restaurante":
                holder.icono.setImageResource(R.drawable.tipo_icon_restaurant);
                break;
            default:
                holder.icono.setImageResource(R.drawable.ic_locationicon);
        }

        // 👉 CLICK: lanzar la actividad
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ShowActivity.class);
            Log.d("LugarID", "El valor es: " + lugar.getId());
            intent.putExtra("lugarId", lugar.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaLugares.size();
    }

    public static class LugarViewHolder extends RecyclerView.ViewHolder {
        TextView textNombre, textDireccion;
        ImageView icono;
        RatingBar ratingBar;

        public LugarViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombre = itemView.findViewById(R.id.nombre);
            textDireccion = itemView.findViewById(R.id.direccion);
            icono = itemView.findViewById(R.id.iconreycler);
            ratingBar = itemView.findViewById(R.id.valoracion);
        }
    }

    public void actualizarLista(List<Lugar> nuevaLista) {
        this.listaLugares = nuevaLista;
        notifyDataSetChanged();
    }
}
