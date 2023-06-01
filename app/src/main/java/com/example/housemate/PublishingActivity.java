package com.example.housemate;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class PublishingActivity extends AppCompatActivity {

    Button openDialog, cancelButton, publishButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publishing);

        openDialog = findViewById(R.id.openDialogButton);

        openDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openDialog();

            }
        });

    }

    private void openDialog() {

        Dialog dialog = new Dialog(PublishingActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View customDialog = inflater.inflate(R.layout.fragment_publishing, null);

        cancelButton = customDialog.findViewById(R.id.cancelButton);
        publishButton = customDialog.findViewById(R.id.publishButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(customDialog);
        dialog.show();

    }
}