package model.profile;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.android.bluetoothscanner.R;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Class for user profile
 */
public class UserProfile {

    private final Context context;
    SharedPreferences sharedPreferences;
    private static final String USERNAME = "username";

    private ImageView profilePictureView;
    private Bitmap bitmap;
    private boolean isProfilePicSet = false;

    private TextView usernameView;
    private Button changeUsernameBtn;
    private String username;
    private boolean isUsernameSet = false;

    public UserProfile(Context context) {
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        initializeUserProfile();
    }

    private void initializeUserProfile() {
        profilePictureView = ((Activity) context).findViewById(R.id.profile_pic_v1);
        usernameView = ((Activity) context).findViewById(R.id.username_v1);
        changeUsernameBtn = ((Activity) context).findViewById(R.id.change_username);

        profilePictureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });
        loadProfilePicture();

        username = sharedPreferences.getString(USERNAME, "USERNAME");
        isUsernameSet = sharedPreferences.contains(USERNAME) ? true : false;
        usernameView.setText(username);

        changeUsernameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUsernameInputPopup();
            }
        });
    }

    /**
     * User android-image-cropper to crop the selected image
     */
    private void pickImage() {
        CropImage.activity().start((Activity) this.context);
    }


    /**
     * Create a popup for the user to enter the username
     */
    private void createUsernameInputPopup() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setTitle("Please enter your username");
        final EditText input = new EditText(this.context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // get input
                username = input.getText().toString();
                usernameView.setText(username);
                isUsernameSet = true;
                // save username
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(USERNAME, username);
                editor.apply();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        }).show();
    }

    /**
     * Set profile picture
     */
    private void loadProfilePicture() {
        try {
            ContextWrapper cw = new ContextWrapper(((Activity) this.context).getApplicationContext());
            File directory = cw.getDir("profile", Context.MODE_PRIVATE);
            String path = directory.getAbsolutePath();
            File file = new File(path, "profile.png");
            if (file.exists()) {
                bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                profilePictureView.setImageBitmap(bitmap);
                isProfilePicSet = true;
            } else {
                // file does not exist, set to default profile picture
                profilePictureView.setImageResource(R.drawable.default_profile_pic);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean isProfilePicSet() { return this.isProfilePicSet; }
    public Bitmap getBitmap() { return this.bitmap; }
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        profilePictureView.setImageBitmap(bitmap);
    }

    public boolean isUsernameSet() { return this.isUsernameSet; }
    public String getUsername() { return this.username; }
}
