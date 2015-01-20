package com.example.RMC;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class CustomProgramList extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] program;
    private final Integer[] imageId;
    public CustomProgramList(Activity context,
                             String[] program ,Integer[] imageId) {
        super(context, R.layout.program_name, program);
        this.context = context;
        this.program = program;
        this.imageId = imageId;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.program_name, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(program[position]);
        imageView.setImageResource(imageId[position]);
        return rowView;
    }
}
