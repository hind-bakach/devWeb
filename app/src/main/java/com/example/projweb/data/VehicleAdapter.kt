package com.example.projweb.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projweb.R

class VehicleAdapter(
    private val vehicles: List<Vehicle>,
    private val onClick: (Vehicle) -> Unit
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    inner class VehicleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMarque: TextView = view.findViewById(R.id.tvMarque)
        val tvModele: TextView = view.findViewById(R.id.tvModele)
        val tvImmat: TextView = view.findViewById(R.id.tvImmat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vehicle, parent, false)
        return VehicleViewHolder(view)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val v = vehicles[position]
        holder.tvMarque.text = "${v.marque} ${v.modele}"
        holder.tvModele.text = "Année : ${v.annee}"
        holder.tvImmat.text = v.immatriculation
        holder.itemView.setOnClickListener { onClick(v) }
    }

    override fun getItemCount() = vehicles.size
}