package com.example.modakflix;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PermissionClass extends AppCompatActivity {

    static boolean permissionCompletedFlag = false;
    static int REQUEST_CODE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_class);
    }

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    static Context context;
    static Activity activity;
    static boolean flag = false;

    public static boolean permissionFlag = false;
    public PermissionClass(Context context, Activity activity, int REQUEST_CODE) {
        this.context = context;
        this.activity = activity;
        this.REQUEST_CODE = REQUEST_CODE;
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    public static void permission(int flag)
    {
        new PermissionClass(context, activity, REQUEST_CODE);
        if(!PermissionClass.permissionFlag)
        {
            if(flag == 0)
            {
                showDialogOK(activity.getString(R.string.permission_seek_msg), new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.R)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                PermissionClass.checkAndRequestPermissions(activity, context);
                                PermissionClass.permissionFlag = true;
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                permissionCompletedFlag = true;
                                break;
                        }
                    }
                });
            }
            else
            {
                PermissionClass.checkAndRequestPermissions(activity, context);
                PermissionClass.permissionFlag = true;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public static Boolean checkAndRequestPermissions(Activity activity, Context context)
    {
        new PermissionClass(context, activity, REQUEST_CODE);
        int permissionReadStorage = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (permissionReadStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        int permissionWriteStorage;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
        {
            permissionWriteStorage = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        else
        {
            permissionWriteStorage = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE);
            if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public static Boolean checkRequiredPermission(Context context)
    {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
        {
                if(context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                {
                    if(context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    {
                        return true;
                    }

                }

        }
        else
        {

                if(context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                {
                    if(Environment.isExternalStorageManager())
                    {
                        return true;
                    }
                }

        }

        return false;
    }

    public static void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("OK", okListener);
        alertDialogBuilder.setNegativeButton("Cancel", okListener);
        alertDialogBuilder.create();
        alertDialogBuilder.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public boolean getPermission()
    {
        if(!PermissionClass.permissionFlag && Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
        {
            PermissionClass.permission(0);
        }
        else
        {
            getWritePermissionAndroidR();
        }
        return PermissionClass.permissionFlag;
    }
    @RequiresApi(api = Build.VERSION_CODES.R)
    private void getWritePermissionAndroidR()
    {
        if(!PermissionClass.checkRequiredPermission(context))
        {
            showDialogOK(activity.getString(R.string.permission_seek_msg), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                        {
                            showDialogOK("In android 11 for write permission, we need user explicit authentication. Do you want to continue?", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            getAndroid11FileStorageAccess(activity);
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            Toast.makeText(context, "Cannot continue without write permission.", Toast.LENGTH_LONG).show();
                                            //System.exit(0);
                                            permissionCompletedFlag = true;
                                            break;
                                    }
                                }
                            });
                            break;
                        }

                        case DialogInterface.BUTTON_NEGATIVE:
                            Toast.makeText(context, "Cannot continue without write permission.", Toast.LENGTH_LONG).show();
                            //System.exit(0);
                            break;
                    }
                }
            });
        }
    }
    private void getAndroid11FileStorageAccess(Activity act)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {

            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.addCategory("android.intent.category.DEFAULT");
            Uri uri = Uri.fromParts("package", act.getPackageName(), null);
            intent.setData(uri);
            act.startActivityForResult(intent, REQUEST_CODE);
        }
    }

    /*@RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            // There are no request codes
            if(!Environment.isExternalStorageManager())
            {
                Toast.makeText(this, "Cannot continue without write permission.", Toast.LENGTH_LONG).show();
                System.exit(0);
            }
            PermissionClass.permission(1);

        }
    }*/
}