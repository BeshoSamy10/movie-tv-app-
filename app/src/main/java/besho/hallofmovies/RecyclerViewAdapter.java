package besho.hallofmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Collections;
import java.util.List;

/**
 * Created by Besho Samy on 16-Apr-16.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>
{

    LayoutInflater inflater;
    List<Row> rowInfo = Collections.emptyList();
    NavigationDrawerResponder responder;



    boolean firstCall = true;
    public RecyclerViewAdapter (Context context ,List<Row> data,NavigationDrawerResponder responder )
    {

        inflater = LayoutInflater.from(context);
        this.responder=responder;
        this.rowInfo = data;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.navigation_drawer_row,parent,false);
        RecyclerViewHolder viewHolder = new RecyclerViewHolder(view);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, final int position) {


        Row current = rowInfo.get(position);
        holder.textView.setText(current.text);
        holder.imageView.setImageResource(current.imageId);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                responder.listenToNavigation(position);
            }
        });


    }

    @Override
    public int getItemCount()
    {
        return rowInfo.size();
    }


    class RecyclerViewHolder extends RecyclerView.ViewHolder
    {
        ImageView imageView;
        TextView textView;

        public RecyclerViewHolder(View itemView)
        {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.row_image);
            textView = (TextView) itemView.findViewById(R.id.row_text);
        }


    }

}
