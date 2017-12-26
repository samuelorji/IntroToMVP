package com.example.samuel.recyclermvp.view;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.Fade;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.samuel.recyclermvp.R;
import com.example.samuel.recyclermvp.data.FakeDataSource;
import com.example.samuel.recyclermvp.data.ListItem;
import com.example.samuel.recyclermvp.logic.Controller;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements ViewInterface{

    private List<ListItem> listItems;
    private RecyclerView recyclerView;
    private CustomAdapter adapter;
    private Controller controller;
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.rec_main_activity);

        controller = new Controller(this,new FakeDataSource());
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.createNewItem();
            }
        });
    }
    public View findView(View view,int Id){
        return view.findViewById(Id);
    }


    @Override
    public void startDetailsActivity(ListItem item,View viewRoot) {
        Intent detailsIntent = new Intent(MainActivity.this,DetailsActivity.class);
        detailsIntent.putExtra("item",item);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){

            getWindow().setEnterTransition(new Fade(Fade.IN));
            getWindow().setEnterTransition(new Fade(Fade.OUT));
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(
                    this,
                    new Pair<View, String>(findView(viewRoot,R.id.data_image),getString(R.string.drawable)),
                    new Pair<View, String>(findView(viewRoot,R.id.data_caption),getString(R.string.message)),
                    new Pair<View, String>(findView(viewRoot,R.id.data_date_time),getString(R.string.date_time))
            );
            startActivity(detailsIntent,options.toBundle());
        }
        else{
            startActivity(detailsIntent);
        }


    }

    @Override
    public void setUpAdapterAndView(final List<ListItem> listItems) {
        LinearLayoutManager manager = new LinearLayoutManager(this);

        this.listItems = listItems;
        recyclerView.setLayoutManager(manager);
        adapter = new CustomAdapter();
        recyclerView.setAdapter(adapter);
        DividerItemDecoration decoration = new DividerItemDecoration(recyclerView.getContext(),manager.getOrientation());
        decoration.setDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.divider_white));
        recyclerView.addItemDecoration(decoration);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT){


            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                Log.d(TAG, "onSwiped: item swiped");
                try{

                    int position = viewHolder.getAdapterPosition();
                    controller.OnListItemSwiped(
                            position,
                            listItems.get(position)
                    );
                }
                catch (IndexOutOfBoundsException e){
                    Log.e(TAG, "onSwiped: " +e.getMessage() );
                }

            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void addItemToAdapter(ListItem item) {
        listItems.add(item);
        int endOfList = listItems.size() - 1;
        adapter.notifyItemInserted(endOfList);
        recyclerView.smoothScrollToPosition(endOfList);
    }

    @Override
    public void deleteItemAt(int position) {
       listItems.remove(position);
        adapter.notifyItemRemoved(position);
    }

    @Override
    public void showUndoSnackBar() {
        Snackbar.make(
                findViewById(R.id.root_layout),
                "Item Deleted",
                Snackbar.LENGTH_LONG
        ).setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               controller.OnUndoConfirmed();
            }
        }).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                controller.OnSnackBarDismissed();
            }
        }).show();
    }

    @Override
    public void insertItemAt(int tempItemPosition, ListItem tempItem) {
        listItems.add(tempItemPosition,tempItem);
        adapter.notifyItemInserted(tempItemPosition);
    }

    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder>{



        @Override
        public CustomAdapter.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = getLayoutInflater().inflate(R.layout.item_data,parent,false);
            return new CustomViewHolder(view);


        }

        @Override
        public void onBindViewHolder(CustomAdapter.CustomViewHolder holder, int position) {

            ListItem item = listItems.get(position);
            holder.color.setImageResource(item.getColorResource());
            holder.msg.setText(item.getMessage());
            holder.date.setText(item.getDateTime());
        }

        @Override
        public int getItemCount() {
            return listItems.size();
        }


        public class CustomViewHolder extends RecyclerView.ViewHolder {
            private TextView msg,date;
            private CircleImageView color;
            public CustomViewHolder(View itemView) {
                super(itemView);
                msg = itemView.findViewById(R.id.data_caption);
                date = itemView.findViewById(R.id.data_date_time);
                color = itemView.findViewById(R.id.data_image);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ListItem item = listItems.get(getAdapterPosition());
                        controller.OnListItemClicked(item,view);
                    }
                });
            }
        }
    }
}
