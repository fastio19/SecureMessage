package com.example.securemessage.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securemessage.EncryptionDecryptionHybrid.EncDeHybrid;
import com.example.securemessage.Models.MessageModel;
import com.example.securemessage.R;
import com.example.securemessage.utils.AESUtils;
import com.google.firebase.auth.FirebaseAuth;

import java.sql.Time;
import java.sql.Timestamp;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;

public class ChatAdapter extends RecyclerView.Adapter{
    ArrayList<MessageModel> messageModels;
    Context context;
    int SENDER_VIEW_TYPE=1;
    int RECEIVER_VIEW_TYPE=2;

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context) {
        this.messageModels = messageModels;
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if(viewType==SENDER_VIEW_TYPE){
            View view= LayoutInflater.from(context).inflate(R.layout.sample_sender,parent,
                    false);
            return new SenderViewHolder(view);
        }
        else{
            View view= LayoutInflater.from(context).inflate(R.layout.sample_receiver,parent,
                    false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(messageModels.get(position).getuId().equals(FirebaseAuth.getInstance().getUid())){
            return SENDER_VIEW_TYPE;
        }
        else{
            return RECEIVER_VIEW_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        MessageModel messageModel=messageModels.get(position);
        String decrypted="";
        try {
            decrypted= AESUtils.decrypt(messageModel.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(holder.getClass()==SenderViewHolder.class){
            ((SenderViewHolder)holder).senderMsg.setText(decrypted);
            Time time=new Time(messageModel.getTimeStamp());
            ((SenderViewHolder)holder).senderTime.setText(time.toString());
        }
        else{
            ((ReceiverViewHolder)holder).recieverMsg.setText(decrypted);
            Time time=new Time(messageModel.getTimeStamp());
            ((ReceiverViewHolder)holder).recieverTime.setText(time.toString());
        }
    }

    @Override
    public int getItemCount() {
        return messageModels.size();
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder{

        TextView recieverMsg,recieverTime;
        public ReceiverViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            recieverMsg=itemView.findViewById(R.id.receiverText);
            recieverTime=itemView.findViewById(R.id.receiverTime);
        }
    }
    public class SenderViewHolder extends RecyclerView.ViewHolder{
        TextView senderMsg,senderTime;
        public SenderViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            senderMsg=itemView.findViewById(R.id.senderText);
            senderTime=itemView.findViewById(R.id.senderTime);
        }
    }
}
