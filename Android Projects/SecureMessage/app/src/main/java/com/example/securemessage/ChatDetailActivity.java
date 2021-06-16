package com.example.securemessage;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.securemessage.Adapter.ChatAdapter;
import com.example.securemessage.EncryptionDecryptionHybrid.EncDeHybrid;
import com.example.securemessage.Models.MessageModel;
import com.example.securemessage.databinding.ActivityChatDetailBinding;
import com.example.securemessage.utils.AESUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import javax.crypto.SecretKey;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChatDetailActivity extends AppCompatActivity {
    ActivityChatDetailBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();


        final String senderId=auth.getUid();
        final String receiverId=getIntent().getStringExtra("userId");
        final String userName=getIntent().getStringExtra("userName");
        final String profilePic=getIntent().getStringExtra("profilePic");


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
        FirebaseMessaging.getInstance().subscribeToTopic("all");
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        // Get new FCM registration token
                        token = task.getResult();
                        Log.d("UserToken","Token : "+token);

                    }
                });
        /*
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            String  token = Objects.requireNonNull(task.getResult()).getToken();

                        }

                    }
                });
                */


        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.enterMessage.getText().toString().isEmpty()){
                    binding.enterMessage.setError("Empty Message");
                    return;
                }
                String title=userName;
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
                                if(!title.toString().isEmpty() && !message.toString().isEmpty()
                                && !token.toString().isEmpty()){
                                    FcmNotificationsSender notificationsSender=new
                                            FcmNotificationsSender(token,
                                            title,message,getApplicationContext(),
                                            ChatDetailActivity.this);
                                    notificationsSender.SendNotifications();
                                }
                                else{
                                    Toast.makeText(ChatDetailActivity.this,
                                            "Error Happened !!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        });

    }
}