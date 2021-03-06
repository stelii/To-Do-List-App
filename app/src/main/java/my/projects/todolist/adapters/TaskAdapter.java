package my.projects.todolist.adapters;

import android.graphics.Paint;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import my.projects.todolist.R;
import my.projects.todolist.database.Task;

import static android.content.ContentValues.TAG;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder>{
    private OnCheckboxListener mcheckboxListener ;
    private OnItemClickListener mOnItemClickListener;

    private AsyncListDiffer<Task> mDiffer = new AsyncListDiffer<Task>(this,DIFF_CALLBACK);


    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            //daca niciunul dintre elemente nu are descriere
            //fac comparatie intre ele folosind numele
                if(oldItem.getDescription() == null && newItem.getDescription() == null)
                    return oldItem.getName().equals(newItem.getName()) ;
                //daca unul dintre elemente nu are descriere atunci nu mai are rost sa le compar si returnez fals
                if (oldItem.getDescription() == null || newItem.getDescription() == null) return false ;

                //iar daca ambele au descriere atunci le compar in mod complet
                return oldItem.getName().equals(newItem.getName()) && oldItem.getDescription().equals(newItem.getDescription());
        }
    };

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.task_item,parent,false);

        TaskViewHolder taskViewHolder = new TaskViewHolder(itemView,mOnItemClickListener);
        return taskViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = mDiffer.getCurrentList().get(position);
        holder.displayItem(task);
        Log.d(TAG, "onBindViewHolder: ");

        //TODO : Add functionality to sort the list depending on priority ?? idk
    }

    @Override
    public int getItemCount() {
        return mDiffer.getCurrentList().size();
    }

    public void setCheckboxListener(OnCheckboxListener listener){
        mcheckboxListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mOnItemClickListener = listener;
    }

    public Task getItemAt(int position){
        return mDiffer.getCurrentList().get(position);
    }

    public void submitList(List<Task> submittedTasks){
        mDiffer.submitList(submittedTasks);
//        taskListFiltered = new ArrayList<>(submittedTasks);
    }

    public List<Task> getTasks(){
        return mDiffer.getCurrentList();
    }

//    @Override
//    public Filter getFilter() {
//        return filter;
//    }
//
//    Filter filter = new Filter() {
//        @Override
//        protected FilterResults performFiltering(CharSequence constraint) {
////            //logic of filtering goes here
////            List<Task> filteredList = new ArrayList<>();
////
////            if(constraint.toString().isEmpty()){
////                Log.d(TAG, "performFiltering: " + "is empty???");
////                filteredList.addAll(mDiffer.getCurrentList());
////            }else{
////                Log.d(TAG, "performFiltering: " + "not emptyY???");
////                for(Task t : taskListFiltered){
////                    if(t.getName().toLowerCase().contains(constraint.toString().toLowerCase())){
////                        filteredList.add(t);
////                    }
////                }
////            }
////
////            FilterResults filterResults = new FilterResults();
////            filterResults.values = filteredList;
////
////            return filterResults;
//        }
//
//        @Override
//        protected void publishResults(CharSequence constraint, FilterResults results) {
////            taskListFiltered.clear();
////            taskListFiltered.addAll((Collection<? extends Task>) results.values);
////            submitList(taskListFiltered);
//        }
//    };


    public class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTaskName;
        private CheckBox mCheckBox;
        private ImageView mPriorityArrow ;
        private TextView mDueDate ;
        private TextView mTaskDescription;

        private OnItemClickListener mOnItemClickListener;

        public TaskViewHolder(@NonNull View itemView,OnItemClickListener listener) {
            super(itemView);
            mOnItemClickListener = listener ;
            itemView.setOnClickListener(this);

            mTaskName = itemView.findViewById(R.id.task_item_name);
            mCheckBox = itemView.findViewById(R.id.task_item_checkbox);
            mPriorityArrow = itemView.findViewById(R.id.task_item_priority_icon);
            mDueDate = itemView.findViewById(R.id.task_item_due_date);
            mTaskDescription = itemView.findViewById(R.id.task_item_description);

            mCheckBox.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    setItemStatus();
                }
            });
        }

        public void displayItem(Task task){
            mTaskName.setText(task.getName());

            if (task.isDone()) {
                mTaskName.setPaintFlags(mTaskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                mCheckBox.setChecked(true);
            } else {
                mTaskName.setPaintFlags(mTaskName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                mCheckBox.setChecked(false);
            }
            mPriorityArrow.setColorFilter(task.getPriority().getColor());

            if(task.isDone()) {
                mDueDate.setVisibility(View.VISIBLE);
                mTaskDescription.setVisibility(View.INVISIBLE);
            }else{
                Log.d(TAG, "displayItem: " + "in ramura de else");
                mTaskDescription.setText(task.getDescription());
            }

        }

        private String getDateAsString(Date date){
            return DateFormat.format("dd/MM/yyyy HH:mm",date.getTime()).toString();
        }

        private void setItemStatus(){
            int position = getAdapterPosition();
            Task task = getItemAt(position);

            boolean taskStatus = mcheckboxListener.changeItemStatus(task);
            if (taskStatus){
                mTaskName.setPaintFlags(mTaskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                mTaskDescription.setVisibility(View.GONE);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                String hour = simpleDateFormat.format(new Date());

                mDueDate.setText("Done at " + hour);
                mDueDate.setVisibility(View.VISIBLE);
            }
            else {
                mTaskName.setPaintFlags(mTaskName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                mTaskDescription.setText(task.getDescription());
                mTaskDescription.setVisibility(View.VISIBLE);
                mDueDate.setVisibility(View.INVISIBLE);

            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Task task = getItemAt(position);
            this.mOnItemClickListener.onItemShortClick(task);
        }
    }


    public interface OnCheckboxListener{
        boolean changeItemStatus(Task task);
    }

    public interface OnItemClickListener{
        void onItemShortClick(Task task);
    }
}
