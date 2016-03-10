package com.example.igorklimov.tictactoe.wifi;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.igorklimov.tictactoe.R;
import com.example.igorklimov.tictactoe.databinding.FragmentPlayersListBinding;
import com.example.igorklimov.tictactoe.databinding.PlayerRowBinding;
import com.iklimov.tictactoe.backend.game.model.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayersListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<Player>> {
    private static final int LOADER_ID = 100;
    private static final String LOG_TAG = "PlayersListFragment";

    private OnFragmentInteractionListener mListener;
    private Context mContext;
    Adapter adapter;

    public PlayersListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentPlayersListBinding mBinding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_players_list, container, false);
        mContext = getContext();
        mBinding.recycler.setLayoutManager(new LinearLayoutManager(mContext));
        adapter = new Adapter();
        mBinding.recycler.setAdapter(adapter);
        getLoaderManager().initLoader(LOADER_ID, null, this);
        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<List<Player>> onCreateLoader(int id, Bundle args) {
        return new GetOnlinePlayers(mContext);
    }

    @Override
    public void onLoadFinished(Loader<List<Player>> loader, List<Player> data) {
        if (data != null) {
            adapter.setPlayers(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Player>> loader) {

    }

    class Adapter extends RecyclerView.Adapter<Holder> {
        List<Player> players;

        public Adapter() {
            players = new ArrayList<>();
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext());

            PlayerRowBinding binding = DataBindingUtil
                    .inflate(inflater, R.layout.player_row, parent, false);

            return new Holder(binding.getRoot());
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            Player player = players.get(position);
            holder.binding.setPlayer(player);
        }

        @Override
        public int getItemCount() {
            return players == null ? 0 : players.size();
        }

        public void setPlayers(List<Player> players) {
            this.players = players;
            notifyDataSetChanged();
        }
    }

    private class Holder extends RecyclerView.ViewHolder {
        PlayerRowBinding binding;

        public Holder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Player player = binding.getPlayer();
                    inviteToPlay(player);
                }
            });
        }
    }

    private static class GetOnlinePlayers extends AsyncTaskLoader<List<Player>> {
        private List<Player> players;

        public GetOnlinePlayers(Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            if (players == null) {
                forceLoad();
            } else {
                deliverResult(players);
            }
        }

        @Override
        public List<Player> loadInBackground() {
            try {
                return Utils.sRegService.listOnlinePlayers(Utils.sUserId).execute().getItems();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void deliverResult(List<Player> data) {
            super.deliverResult(data);
            players = data;
        }
    }

    private class InviteToPlay extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String yourId = params[0];
            String opponentsId = params[1];
            try {
                Utils.sRegService.inviteToPlay(yourId, opponentsId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void inviteToPlay(final Player player) {
        new AlertDialog.Builder(mContext)
                .setMessage(
                        mContext.getString(
                                R.string.format_send_invitation_to_play, player.getName()))
                .setPositiveButton(
                        mContext.getString(
                                R.string.send), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new InviteToPlay().execute(Utils.sUserId, player.getRegId());
                            }
                        })
                .setNeutralButton(
                        mContext.getString(
                                R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                .show();
    }

}
