package com.sd.vr.education.view;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sd.vr.R;
import com.sd.vr.education.VREducationMainActivity;
import com.sd.vr.education.presenter.FilesManager;
import com.sd.vr.education.vrplayer.VideoPlayerActivity;

/**
 * Created by hl09287 on 2017/5/3.
 */

public class VideoGridViewAdapter extends BaseAdapter {

    private List listVideo;
    public VREducationMainActivity context;
    public VideoGridViewAdapter(List list, VREducationMainActivity context){
        listVideo = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return listVideo.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.video_item, parent, false);
        TextView textView = (TextView) relativeLayout.findViewById(R.id.video_title);
        final String fileId = listVideo.get(position).toString();
        ImageView bofang = (ImageView) relativeLayout.findViewById(R.id.bofang);
        ImageView shanchu = (ImageView) relativeLayout.findViewById(R.id.shanchu);
        bofang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = FilesManager.DIRECTORY+"/"+ fileId;
                Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra("START",url);
                context.startActivity(intent);
            }
        });

        shanchu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilesManager.getInstance().deleteFile(fileId);
                context.updateUI();
            }
        });

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = FilesManager.DIRECTORY+"/"+ fileId;
                Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra("START",url);
                context.startActivity(intent);
            }
        });

        textView.setText(fileId);
        return relativeLayout;
    }
}
