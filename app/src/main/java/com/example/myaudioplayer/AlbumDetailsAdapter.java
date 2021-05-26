package com.example.myaudioplayer;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class AlbumDetailsAdapter extends RecyclerView.Adapter<AlbumDetailsAdapter.MyHolder> {
    private Context mContext;
    static ArrayList<MusicFiles> albumFiles;
    View view;
    public AlbumDetailsAdapter(Context mContext, ArrayList<MusicFiles> albumFiles) {
        this.mContext = mContext;
        this.albumFiles = albumFiles;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new MyHolder(view) ;
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        holder.album_song.setText(albumFiles.get(position).getTitle());
        byte[] image = getAlbumArt(albumFiles.get(position).getPath());

        if (image != null){
            Glide.with(mContext).asBitmap()
                    .load(image)
                    .into(holder.album_image);
        }else{
            Glide.with(mContext)
                    .load(R.drawable.defaultdisk)
                    .into(holder.album_image);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("sender", "albumDetails");
                intent.putExtra("position", position);
                mContext.startActivity(intent);
            }
        });

        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.delete:
                                //Delete:
                                //Toast.makeText(mContext, "Delete Clicked!!", Toast.LENGTH_SHORT).show();
                                deleteFile(position, view);
                                break;
                        }
                        return true;
                    }
                });
            }
        });
    }


    private void deleteFile(int position, View view){
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(albumFiles.get(position).getId()));
        File file = new File(albumFiles.get(position).getPath());
        boolean deleted = file.delete(); // Delete the file
        Log.e("path:", ""+file.getAbsolutePath());
        if (deleted){
            mContext.getContentResolver().delete(contentUri, null, null);
            albumFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, albumFiles.size());
            Snackbar.make(view, "File Deleted!", Snackbar.LENGTH_LONG).show();
        }else{
            // maybe file is in the sd card
            Snackbar.make(view, "Can't be deleted! ", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        ImageView album_image, menuMore;
        TextView album_song;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            album_image = itemView.findViewById(R.id.music_img);
            album_song = itemView.findViewById(R.id.music_file_name);
            menuMore = itemView.findViewById(R.id.menuMore);
        }
    }

    private byte[] getAlbumArt(String uri){
        FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever(); //He utilizado otro para solventar los problemas de Glide
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();

        return art;
    }
}
