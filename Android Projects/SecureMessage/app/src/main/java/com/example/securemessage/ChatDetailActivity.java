package com.example.securemessage;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.securemessage.Adapter.ChatAdapter;
import com.example.securemessage.EncryptionDecryptionHybrid.EncDeHybrid;
import com.example.securemessage.Models.MessageModel;
import com.example.securemessage.SendNotifications.NotiModel.Notification;
import com.example.securemessage.SendNotifications.NotiModel.NotificationReq;
import com.example.securemessage.SendNotifications.NotiModel.NotificationResponse;
import com.example.securemessage.SendNotifications.NotificationRequest;
import com.example.securemessage.SendNotifications.RetrofitClient;
import com.example.securemessage.databinding.ActivityChatDetailBinding;
import com.example.securemessage.utils.AESUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;

import javax.crypto.SecretKey;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.securemessage.SendNotifications.Constants.BASE_URL;

public class ChatDetailActivity extends AppCompatActivity {
    ActivityChatDetailBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();


        final String senderId=auth.getUid();
        String receiverId=getIntent().getStringExtra("userId");
        String userName=getIntent().getStringExtra("userName");
        String profilePic=getIntent().getStringExtra("profilePic");


        binding.userName.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.ic_user).into(binding.profileImage);
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ChatDetailActivity.this,MainActivity.class);
                startActivity(intent);
                
            }
        });

        final ArrayList<MessageModel> messageModels=new ArrayList<>();
        final ChatAdapter chatAdapter=new ChatAdapter(messageModels,this,receiverId);
        binding.chatRecyclerView.setAdapter(chatAdapter);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        final String senderRoom=senderId+receiverId;
        final String receiverRoom=receiverId+senderId;
        System.out.println(senderId+" "+receiverId);
        database.getReference().child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        messageModels.clear();
                        for(DataSnapshot snapshot1:snapshot.getChildren()){
                            MessageModel model=snapshot1.getValue(MessageModel.class);
                            model.setMessageId(snapshot1.getKey());
                            messageModels.add(model);
                        }
                        chatAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            chatAdapter.notifyDataSetChanged();

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.enterMessage.getText().toString().isEmpty()){
                    binding.enterMessage.setError("Empty Message");
                    return;
                }
                String message=binding.enterMessage.getText().toString();
                String key="";
                String encrypted="";
                try{
                    encrypted= AESUtils.encrypt(message);
                    key=new String(AESUtils.getRawKey());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final MessageModel model=new MessageModel(encrypted,senderId,key);
                model.setTimeStamp(new Date().getTime());
                binding.enterMessage.setText("");
                database.getReference().child("chats")
                        .child(senderRoom).push()
                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        database.getReference().child("chats").child(receiverRoom)
                                .push()
                                .setValue(model).addOnSuccessListener
                                (new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(ChatDetailActivity.this,
                                        "Success", Toast.LENGTH_SHORT).show();
                                database.getReference().child("Users").child(senderId)
                                        .child("Info").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                        String token=snapshot.child("token").getValue(String.class);
                                        NotificationReq req=new NotificationReq(token,
                                                new NotificationReq.Notification(userName,
                                                        userName+" : "+message));
                                        RetrofitClient.getRetrofit(BASE_URL)
                                                .create(NotificationRequest.class)
                                                .sent(req)
                                                .enqueue(new Callback<NotificationResponse>() {
                                                    @Override
                                                    public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {

                                                    }

                                                    @Override
                                                    public void onFailure(Call<NotificationResponse> call, Throwable t) {

                                                    }
                                                });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });
                            }
                        });
                    }
                });
            }
        });

    }
}