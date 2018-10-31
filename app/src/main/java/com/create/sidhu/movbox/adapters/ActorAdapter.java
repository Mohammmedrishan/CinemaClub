package com.create.sidhu.movbox.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.create.sidhu.movbox.R;
import com.create.sidhu.movbox.fragments.HomeFragment;
import com.create.sidhu.movbox.helpers.StringHelper;
import com.create.sidhu.movbox.models.ActorModel;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
//TODO: Character name incorporation
/**
 * Adapter for Actor Recycler View
 */

public class ActorAdapter extends RecyclerView.Adapter<ActorAdapter.ViewHolder>{
    private ArrayList<ActorModel> actorModels;
    private Context context;
    private View rootview;


    public ActorAdapter(Context context, ArrayList<ActorModel> actorModels, View rootview) {
        this.context = context;
        this.actorModels = actorModels;
        this.rootview = rootview;
    }


    @NonNull
    @Override
    public ActorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cast_individual_listitem,parent,false);
        return new ActorAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActorAdapter.ViewHolder holder, final int position)  {
        Glide.with(context)
                .asBitmap()
                .load(actorModels.get(position).getImage())
                .into(holder.imgDefinitionImage);
        holder.imgDefinitionImageType.setImageResource(actorModels.get(position).getType() == null || actorModels.get(position).getType().equalsIgnoreCase("director")? R.drawable.ic_director: R.drawable.ic_actor);
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        SpannableString spannableString = new SpannableString(actorModels.get(position).getName());
        spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.colorTextPrimary)), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        stringBuilder.append(spannableString);
        spannableString = new SpannableString(" (" + StringHelper.roundFloat(actorModels.get(position).getRating(), 1) + "/10)");
        spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.colorTextSecondary)), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        stringBuilder.append(spannableString);
        holder.textViewDefinitionName.setText(stringBuilder);
        holder.llCastMaster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HomeFragment homeFragment = new HomeFragment();
                homeFragment.OnClick(position, context, rootview, actorModels, view, "cast");
            }
        });

    }


    @Override
    public int getItemCount() {
        return actorModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView imgDefinitionImage;
        CircleImageView imgDefinitionImageType;
        TextView textViewDefinitionName;
        LinearLayout llCastMaster;
        Typeface tfSemibold = Typeface.createFromAsset(context.getAssets(), "fonts/MyriadPro-Semibold.otf");
        Typeface tfRegular = Typeface.createFromAsset(context.getAssets(), "fonts/myriadpro.otf");
        public ViewHolder(View itemView) {
            super(itemView);
            imgDefinitionImage = itemView.findViewById(R.id.img_Cast);
            imgDefinitionImageType = itemView.findViewById(R.id.img_CastType);
            textViewDefinitionName = itemView.findViewById(R.id.textView_indvDefinitionName);
            llCastMaster = itemView.findViewById(R.id.containerIndvCastMaster);
            textViewDefinitionName.setTypeface(tfSemibold);
        }
    }
}
