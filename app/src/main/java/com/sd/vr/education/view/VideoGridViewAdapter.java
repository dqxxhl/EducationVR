package com.sd.vr.education.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sd.vr.R;
import com.sd.vr.education.VREducationMainActivity;
import com.sd.vr.education.entity.VideoItem;
import com.sd.vr.education.presenter.FilesManager;
import com.sd.vr.education.vrplayer.VideoPlayerActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hl09287 on 2017/5/3.
 */

public class VideoGridViewAdapter extends BaseAdapter {

    private List<VideoItem> listVideo;
    public VREducationMainActivity context;
    public VideoGridViewAdapter(List<VideoItem> list, VREducationMainActivity context){
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
    public View getView(final int position, final View convertView, ViewGroup parent) {
        RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.video_item, parent, false);
        TextView textView = (TextView) relativeLayout.findViewById(R.id.video_title);//标题
        ImageView icon_status = (ImageView) relativeLayout.findViewById(R.id.icon_status);//状态
        RelativeLayout shanchu = (RelativeLayout) relativeLayout.findViewById(R.id.layout_shanchu);//删除
        TextView jindu = (TextView) relativeLayout.findViewById(R.id.jindu);//进度
        RelativeLayout layout_chongshi = (RelativeLayout) relativeLayout.findViewById(R.id.layout_chongshi);

        String tip = "是否确认删除该教学资源?";

        final String fileName = listVideo.get(position).fileName;
        final String fileNameShow = listVideo.get(position).fileNameShow;
        final String fileId = listVideo.get(position).fileId;
        if (listVideo.get(position).fileStatus == FilesManager.STATUS_COMPLETE_DOWNLOAD){//下载完成
            //隐藏状态图标
            icon_status.setVisibility(View.GONE);
            //隐藏进度
            jindu.setVisibility(View.GONE);
            //隐藏重试按钮
            layout_chongshi.setVisibility(View.GONE);
            //显示删除按钮
            shanchu.setVisibility(View.VISIBLE);
            //Item可点击
            relativeLayout.setEnabled(true);
        }else if (listVideo.get(position).fileStatus == FilesManager.STATUS_TO_DOWNLOAD){//待下载
            //显示状态图标
            icon_status.setVisibility(View.VISIBLE);
            icon_status.setImageResource(R.drawable.vr_to_dowenload);
            //隐藏进度
            jindu.setVisibility(View.GONE);
            //隐藏重试按钮
            layout_chongshi.setVisibility(View.GONE);
            //显示删除按钮
            shanchu.setVisibility(View.VISIBLE);
            tip = "是否确认删除该下载任务?";
            //Item不可点击
            relativeLayout.setEnabled(false);

        }else if (listVideo.get(position).fileStatus == FilesManager.STATUS_DOWNLOADING){//下载中
            //显示状态图标
            icon_status.setVisibility(View.VISIBLE);
            icon_status.setImageResource(R.drawable.vr_downloading);
            //显示进度
            jindu.setVisibility(View.VISIBLE);
            jindu.setText(listVideo.get(position).progress+"%");
            //隐藏重试按钮
            layout_chongshi.setVisibility(View.GONE);
            //隐藏删除按钮
            shanchu.setVisibility(View.INVISIBLE);
            //Item不可点击
            relativeLayout.setEnabled(false);
        }else if (listVideo.get(position).fileStatus == FilesManager.STATUS_ERROR_DOWNLOAD){//下载异常
            //显示状态图标
            icon_status.setVisibility(View.VISIBLE);
            icon_status.setImageResource(R.drawable.vr_error_download);
            //隐藏进度
            jindu.setVisibility(View.GONE);
            //显示重试按钮
            layout_chongshi.setVisibility(View.VISIBLE);
            //隐藏删除按钮
            shanchu.setVisibility(View.INVISIBLE);
            //Item不可点击
            relativeLayout.setEnabled(false);
        }

        final String finalTip = tip;
        shanchu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("提示"); //设置标题
                builder.setMessage(finalTip);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (listVideo.get(position).fileStatus == FilesManager.STATUS_TO_DOWNLOAD){
                            FilesManager.getInstance().deteTask(fileId);
                        }else {
                            FilesManager.getInstance().deleteFile(fileName);
                        }
                        context.updateUI();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { //设置取消按钮
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        /**
         * 重试
         */
        layout_chongshi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilesManager.getInstance().repty(fileId);
            }
        });

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = FilesManager.DIRECTORY+"/"+ fileName;
                Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra("START",url);
                context.startActivity(intent);
            }
        });

        textView.setText(fileNameShow);
        return relativeLayout;
    }
}
