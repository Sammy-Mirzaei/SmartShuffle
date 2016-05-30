package com.example.sammirzaei.smartshuffle;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by sammirzaei on 4/27/2016.
 */
public class SongsAdapter extends ArrayAdapter<Song>{

    private static Context context;
    private static Song[] songsArray;
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    private static Bitmap blue_note;
    private final Activity activity;

    static class ViewHolder {
        public TextView songNameText;
        public TextView artistText;
        public TextView ratingText;
        public ImageView image;
        public int position;
    }

    private static class ThumbnailTask extends AsyncTask <ViewHolder, Void, Bitmap>{
        private int mPosition;
        private ViewHolder mHolder;

        public ThumbnailTask(int position, ViewHolder holder) {
            mPosition = position;
            mHolder = holder;
        }

        @Override
        protected Bitmap doInBackground(ViewHolder... params) {
            // Download bitmap here
            Uri uri = ContentUris.withAppendedId(sArtworkUri, songsArray[mPosition].getAlbumId());
            Bitmap bitmap = blue_note;
            try{
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            }catch(IOException ex){
                ex.printStackTrace();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (mHolder.position == mPosition) {
                mHolder.image.setImageBitmap(bitmap);
            }
        }
    }

    public SongsAdapter(Activity activity, Context context, ArrayList<Song> songs) {
        super(context, R.layout.song_row_layout, songs);
        this.context = context;
        this.blue_note = BitmapFactory.decodeResource(context.getResources(),R.drawable.blue_music_note);
        this.songsArray = songs.toArray(new Song[songs.size()]);
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.song_row_layout, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.songNameText = (TextView) rowView.findViewById(R.id.label);
            viewHolder.artistText = (TextView) rowView.findViewById(R.id.artist_label);
            viewHolder.ratingText = (TextView) rowView.findViewById(R.id.rating_label);
            viewHolder.image = (ImageView) rowView.findViewById(R.id.song_row_image);
            rowView.setTag(viewHolder);

            System.gc();

        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.position = position;
        new ThumbnailTask(position, holder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
        holder.songNameText.setText(songsArray[position].getName());
        holder.artistText.setText(songsArray[position].getArtist());
        holder.ratingText.setText(String.valueOf(songsArray[position].getImportance()));
        return rowView;

    }

}
