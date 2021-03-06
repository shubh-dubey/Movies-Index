package com.sg.moviesindex.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.dd.processbutton.ProcessButton;
import com.sg.moviesindex.R;
import com.sg.moviesindex.databinding.TorrentListItemsLayoutBinding;
import com.sg.moviesindex.model.yts.Torrent;
import com.sg.moviesindex.service.TorrentFetcherService;

import java.util.List;


public class TorrentsListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<Torrent> torrentList;
    private ProcessButton button;
    private TorrentFetcherService.OnCompleteListener completeListener;

    public TorrentsListItemAdapter(Context context, List<Torrent> torrentList, ProcessButton button, TorrentFetcherService.OnCompleteListener completeListener) {
        this.context = context;
        this.torrentList = torrentList;
        this.button=button;
        this.completeListener=completeListener;
    }

    @BindingAdapter({"boldText","normalText"})
    public static void format(TextView textView, String boldText, String normalText){
        SpannableString str = new SpannableString(boldText + normalText);
        str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(str);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TorrentListItemsLayoutBinding torrentListItemsLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.torrent_list_items_layout, parent, false);
        return new TorrentListItemViewHolder(torrentListItemsLayoutBinding,button,completeListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TorrentListItemViewHolder) {
            Torrent torrent =torrentList.get(position);
            ((TorrentListItemViewHolder)holder).torrentListItemsLayoutBinding.setTorrent(torrent);
        }
    }

    @Override
    public int getItemCount() {
        return torrentList == null? 0 : torrentList.size();
    }


    static class TorrentListItemViewHolder extends RecyclerView.ViewHolder {
        private TorrentListItemsLayoutBinding torrentListItemsLayoutBinding;
        private ProcessButton button;
        private TorrentFetcherService.OnCompleteListener completeListener;

        TorrentListItemViewHolder(@NonNull final TorrentListItemsLayoutBinding torrentListItemsLayoutBinding,ProcessButton button, TorrentFetcherService.OnCompleteListener completeListener) {
            super(torrentListItemsLayoutBinding.getRoot());
            this.button=button;
            this.completeListener=completeListener;
            this.torrentListItemsLayoutBinding=torrentListItemsLayoutBinding;
            torrentListItemsLayoutBinding.ivDownloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TorrentFetcherService.resultantTorrent=torrentListItemsLayoutBinding.getTorrent();
                    button.setProgress(100);
                    completeListener.onComplete(false);
                }
            });
        }

    }
}
