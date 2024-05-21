package com.cs360.project3;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class InventoryAdapter extends BaseAdapter {

    private Context context;
    private List<InventoryEntry> inventoryEntries;
    private SparseBooleanArray selectedItems;

    public InventoryAdapter(Context context, List<InventoryEntry> inventoryEntries) {
        this.context = context;
        this.inventoryEntries = inventoryEntries;
        this.selectedItems = new SparseBooleanArray();
    }


    @Override
    public int getCount() {
        return inventoryEntries.size();
    }

    @Override
    public Object getItem(int position) {
        return inventoryEntries.get(position);
    }

    public void removeItem(InventoryEntry entry) {
        inventoryEntries.remove(entry);
    }

    @Override
    public long getItemId(int position) {
        InventoryEntry entry = (InventoryEntry) getItem(position);
        return entry.getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("getView position", String.valueOf(position));

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.inventory_item, parent, false);
            holder = new ViewHolder();
            holder.itemNameTextView = convertView.findViewById(R.id.itemNameTextView);
            holder.quantityTextView = convertView.findViewById(R.id.quantityTextView);
            holder.checkBox = convertView.findViewById(R.id.checkBox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        InventoryEntry entry = inventoryEntries.get(position);

        holder.itemNameTextView.setText(entry.getItemName());
        holder.quantityTextView.setText(String.valueOf(entry.getQuantity()));
        holder.checkBox.setChecked(selectedItems.get(position, false));

        // Add or update the click listener for the CheckBox
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Toggle the selection when the CheckBox state changes
            toggleSelection(position);
            Log.d("Checkbox clicked", String.valueOf(position));
        });

        return convertView;
    }


    // Method to get selected items
    public List<InventoryEntry> getSelectedItems() {
        Log.d("Selected count", "" + selectedItems.size());
        List<InventoryEntry> selected = new ArrayList<>();
        for (int i = 0; i < inventoryEntries.size(); i++) {
            if (selectedItems.get(i, false)) {
                selected.add(inventoryEntries.get(i));
            }
        }
        return selected;
    }

    // Method to toggle selection for a given position
    public void toggleSelection(int position) {
        selectedItems.put(position, !selectedItems.get(position, false));
        notifyDataSetChanged();
        Log.d("InventoryAdapter", "Item at position " + position + " selected: " + selectedItems.get(position));
    }


    // Helper class to hold views in each list item
    private static class ViewHolder {
        TextView itemNameTextView;
        TextView quantityTextView;
        CheckBox checkBox;
    }
}
