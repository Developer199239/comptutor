package com.example.comptutor;
import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;

public class ViewHolder extends RecyclerView.ViewHolder {
    View mView;
    ExoPlayer exoPlayer;
    StyledPlayerView mExoPlayerView;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        mView = itemView;
    }
    public void setVideo(Application application, String title,String videourl)
    {
        TextView mtextView = mView.findViewById(R.id.titleVideo);
        mExoPlayerView = mView.findViewById(R.id.exoPlayerView);

        mtextView.setText(title);
        try{
            TrackSelector trackSelector = new DefaultTrackSelector(application);
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(application).build();
            exoPlayer = new ExoPlayer.Builder(application)
                    .setTrackSelector(trackSelector)
                    .setBandwidthMeter(bandwidthMeter)
                    .build();
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(application,"video");
            Uri videoUrl = Uri.parse(videourl);
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(videoUrl));
            mExoPlayerView.setPlayer(exoPlayer);
            exoPlayer.setMediaSource(mediaSource);
            exoPlayer.setPlayWhenReady(true);
        }catch (Exception e)
        {
            Log.e("ViewHolder","ExoPlayer Error" + e.toString());

        }
    }
}
