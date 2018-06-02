package com.gegejiejie.inventory;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import java.lang.reflect.Array;
import java.util.ArrayList;
import android.provider.BaseColumns;

public class InventoryObject implements Parcelable {
    protected int emp_id = -1;
    protected int store_id = -1;
    protected int curr_prod_id = -1;
    protected int curr_quantity = 0;
    protected ArrayList<Integer> products_list = new ArrayList<Integer>();
    protected ArrayList<Integer> quantity_list = new ArrayList<Integer>();
    protected InventoryObject() {}


    public String toString()
    {
        String r = "Your Employee ID is " + "\"" + Integer.toString(emp_id) + "\"\n";
        r += "Your Store ID is " + "\"" + Integer.toString(store_id) + "\"\n";
        r += "You have \"" + Integer.toString(products_list.size()) + " \" products." + "\n";
        for (int i = 0; i < products_list.size(); ++i)
        {
            int productid = products_list.get(i);
            int quantity = quantity_list.get(i);
            r += "Product ID \"" + Integer.toString(productid) + "\"";
            r += ", Quantity \"" + Integer.toString(quantity) + "\"";
            r += "\n";
        }
        return r;
    }
    public String ProdQuantToString()
    {
        String r = null;
        for (int i = 0; i < products_list.size(); ++i)
        {
            int productid = products_list.get(i);
            int quantity = quantity_list.get(i);
            r += Integer.toString(productid);
            r += "," + Integer.toString(quantity);
            r += ";";
        }
        return r;
    }
    // 99.9% of the time you can just ignore this
    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(emp_id);
        out.writeInt(store_id);
        out.writeInt(curr_prod_id);
        out.writeInt(curr_quantity);
        out.writeList(products_list);
        out.writeList(quantity_list);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<InventoryObject> CREATOR = new Parcelable.Creator<InventoryObject>() {
        public InventoryObject createFromParcel(Parcel in) {
            return new InventoryObject(in);
        }

        public InventoryObject[] newArray(int size) {
            return new InventoryObject[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private InventoryObject(Parcel in) {
        emp_id = in.readInt();
        store_id = in.readInt();
        curr_prod_id = in.readInt();
        curr_quantity = in.readInt();
        in.readList(products_list, null);
        in.readList(quantity_list, null);

    }
}
