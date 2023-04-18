package com.example.lize_app.adapter;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.lize_app.R;
import com.example.lize_app.ui.central.CentralChartFragment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ChartDataDisplayerAdapter extends RecyclerView.Adapter<ChartDataDisplayerAdapter.DataViewHolder> {

    private final List<CentralChartFragment.LabelData> mLabelDatas = new ArrayList<>();

    float x = 0.0f;
    DecimalFormat df = new DecimalFormat("0");

    private ChartDataDisplayerAdapter.DataItemClickListener mListener;

    View view;

    @Override
    public ChartDataDisplayerAdapter.DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.central_chart_item, parent, false);

        ChartDataDisplayerAdapter.DataViewHolder viewHolder = new ChartDataDisplayerAdapter.DataViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ChartDataDisplayerAdapter.DataViewHolder holder, final int position) {
        CentralChartFragment.LabelData labelData = mLabelDatas.get(position);

        if(!labelData.show) {
            holder.dataCard.setVisibility(View.GONE);
            return;
        }
        holder.dataCard.setVisibility(View.VISIBLE);

        holder.labelName.setText(holder.resources.getString(R.string.LabelName) + ": " + labelData.labelName);
        holder.labelX.setText(labelData.xLabel + ": ");
        holder.labelY.setText(labelData.yLabel + ": ");

        df.setMaximumFractionDigits(340);

        // X
        holder.dataX.setText(df.format(x));

        // Y
        String yString = "";
        boolean lock = true;
        for (Float y:labelData.getYByX(x)) {
            // Log.e(String.valueOf(y));
            if(lock) { lock = false; }
            else { yString += "\n"; }
            yString += df.format(y);
        }

        // ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_list_item_1, yString.split("\n"));
        // holder.dataYList.setAdapter(adapter);
        holder.dataYList.setText(yString);

    }

    @Override
    public int getItemCount() {
        return mLabelDatas.size();
    }

    public void setX(float X) {
        x = X;
    }

    public CentralChartFragment.LabelData getDataAtIndex(int index) {
        if (index < mLabelDatas.size()) {
            return mLabelDatas.get(index);
        }

        return null;
    }

    public void addData(CentralChartFragment.LabelData labelData) {
        if (labelData != null && !mLabelDatas.contains(labelData)) {
            mLabelDatas.add(labelData);
        }
    }

    public void clearDatas() {
        mLabelDatas.clear();
    }

    public void setDatas(ArrayList<CentralChartFragment.LabelData> LabelDatas) {
        clearDatas();
        for (int i=0; i<LabelDatas.size(); i++) {
            addData(LabelDatas.get(i));
        }
    }

    public void setListener(ChartDataDisplayerAdapter.DataItemClickListener listener) {
        mListener = listener;
    }

    public class DataViewHolder extends RecyclerView.ViewHolder {

        Resources resources;

        androidx.cardview.widget.CardView dataCard;
        TextView labelName;
        TextView labelX;
        TextView dataX;
        TextView labelY;
        TextView dataYList;

        public DataViewHolder(View itemView) {
            super(itemView);
            resources = itemView.getResources();

            dataCard = (androidx.cardview.widget.CardView) itemView.findViewById(R.id.dataCard);
            labelName = (TextView) itemView.findViewById(R.id.labelName);
            labelX = (TextView) itemView.findViewById(R.id.labelX);
            dataX = (TextView) itemView.findViewById(R.id.dataX);
            labelY = (TextView) itemView.findViewById(R.id.labelY);
            dataYList = (TextView) itemView.findViewById(R.id.dataYList);
        }
    }

    public interface DataItemClickListener { }
}
