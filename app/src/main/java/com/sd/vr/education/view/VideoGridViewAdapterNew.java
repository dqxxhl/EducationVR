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
import com.sd.vr.education.VREducationApplication;
import com.sd.vr.education.VREducationMainActivity;
import com.sd.vr.education.entity.VideoFile;
import com.sd.vr.education.presenter.FilesManager;
import com.sd.vr.education.utils.DatabaseManager;
import com.sd.vr.education.vrplayer.VideoPlayerActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.sd.vr.R.id.icon_status;

/**
 * 首页视频展示页面适配器
 * Created by hl09287 on 2017/4/14.
 */

public class VideoGridViewAdapterNew extends BaseAdapter {

    private static final String TIP_DOWN_LOADED = "下载完成";
    private static final String TIP_DOWN_WAITTING = "等待下载";
    private static final String TIP_DOWN_LOADING = "正在下载";
    private static final String TIP_DOWN_BREAK = "下载中断";
    private List<VideoFile> listVideo;
    public VREducationMainActivity activity;
    public VideoGridViewAdapterNew(List<VideoFile> list, VREducationMainActivity context){
        listVideo = list;
        this.activity = context;
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
        RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(activity).inflate(R.layout.video_item_new, parent, false);
        TextView title = (TextView) relativeLayout.findViewById(R.id.tv_videoitem_title);//标题
        TextView title2 = (TextView) relativeLayout.findViewById(R.id.tv_videoitem_title2);//标题
        ImageView tupian = (ImageView) relativeLayout.findViewById(R.id.iv_videoitem_tu);//主图
        ImageView shanchu = (ImageView) relativeLayout.findViewById(R.id.iv_videoitem_delet);//删除
        TextView jindu = (TextView) relativeLayout.findViewById(R.id.tv_videoitem_progress);//进度
        ImageView layout_chongshi = (ImageView) relativeLayout.findViewById(R.id.iv_videoitem_repty);//
        TextView statusTips = (TextView) relativeLayout.findViewById(R.id.tv_videoitem_status);

        String tip = "是否确认删除该教学资源?";

        final String fileName = listVideo.get(position).getFileName();
        final String fileId = listVideo.get(position).getFileId();
        if (listVideo.get(position).fileStatus == FilesManager.STATUS_COMPLETE_DOWNLOAD){//下载完成
            //隐藏状态图标
            statusTips.setText(TIP_DOWN_LOADED);
            //隐藏进度
            jindu.setVisibility(View.INVISIBLE);
            //隐藏重试按钮
            layout_chongshi.setVisibility(View.GONE);
            //显示删除按钮
            shanchu.setVisibility(View.VISIBLE);
            //Item可点击
            relativeLayout.setEnabled(true);
        }else if (listVideo.get(position).fileStatus == FilesManager.STATUS_TO_DOWNLOAD){//待下载
            //显示状态图标
            statusTips.setText(TIP_DOWN_WAITTING);
            //隐藏进度
            jindu.setVisibility(View.INVISIBLE);
            //隐藏重试按钮
            layout_chongshi.setVisibility(View.INVISIBLE);
            //显示删除按钮
            shanchu.setVisibility(View.VISIBLE);
            tip = "是否确认删除该下载任务?";
            //Item不可点击
            relativeLayout.setEnabled(false);

        }else if (listVideo.get(position).fileStatus == FilesManager.STATUS_DOWNLOADING){//下载中
            //显示状态图标
            statusTips.setText(TIP_DOWN_LOADING);
            //显示进度
            jindu.setVisibility(View.VISIBLE);
            jindu.setText(listVideo.get(position).getProgress()+"%");
            //隐藏重试按钮
            layout_chongshi.setVisibility(View.INVISIBLE);
            //显示删除按钮
            shanchu.setVisibility(View.VISIBLE);
            //Item不可点击
            relativeLayout.setEnabled(false);
            tip = "是否确认删除该下载任务?";
        }else if (listVideo.get(position).fileStatus == FilesManager.STATUS_ERROR_DOWNLOAD){//下载异常
            //显示状态图标
            statusTips.setText(TIP_DOWN_BREAK);
            //隐藏进度
            jindu.setVisibility(View.INVISIBLE);
            //显示重试按钮
            layout_chongshi.setVisibility(View.VISIBLE);
            //显示删除按钮
            shanchu.setVisibility(View.VISIBLE);
            //Item不可点击
            relativeLayout.setEnabled(false);
            tip = "是否确认删除该下载任务?";
        }

        final String finalTip = tip;
        shanchu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("提示"); //设置标题
                builder.setMessage(finalTip);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        List<VideoFile> list = DatabaseManager.getInstance().getQueryByWhere(VideoFile.class, "fileId",new String[]{fileId});
                        if (list.size() > 0){
                            DatabaseManager.getInstance().delete(list.get(0));
                        }
                        if (listVideo.get(position).fileStatus == FilesManager.STATUS_COMPLETE_DOWNLOAD){
                            FilesManager.getInstance().deleteFile(fileId);
                        }else {
                            FilesManager.getInstance().deteTask(fileId);
                        }
                        activity.updateUI();
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
                activity.openVideo(listVideo.get(position));
            }
        });

        title.setText(fileName+".MP4");
        String titleTemp = listVideo.get(position).getFileName();
        String title2Name = "《"+titleTemp+"》";
        title2.setText(title2Name);
        String picUrl = listVideo.get(position).getImageUrl();
        if (picUrl != null && !"".equals(picUrl)){
            Picasso.with(VREducationApplication.getInstance()).load(picUrl).into(tupian);
        }

        return relativeLayout;
    }
}
