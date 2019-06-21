package com.empatica.sample;


import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;


import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Upload extends AppCompatActivity {
    private static final String ACCESS_TOKEN = "lY_d3DAmzgAAAAAAAAAAdn7IAKZUPyYJXhaYmllRlCFZwhYgt_m6fNafXq8DcgWK";

    public static void main(String args[]) throws DbxException, IOException {
        // Create Dropbox client
        DbxRequestConfig config = new DbxRequestConfig("dropbox/E4Link");
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

        // Get current account info
        FullAccount account = client.users().getCurrentAccount();
        System.out.println(account.getName().getDisplayName());


        // Get files and folder metadata from Dropbox root directory
        ListFolderResult result = client.files().listFolder("");
        while (true) {
            for (Metadata metadata : result.getEntries()) {
                System.out.println(metadata.getPathLower());
            }

            if (!result.getHasMore()) {
                break;
            }

            result = client.files().listFolderContinue(result.getCursor());
        }

/*
        // Upload "test.txt" to Dropbox
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH:mm");
        String time = format.format(new Date(System.currentTimeMillis()));
        String fileName = "IBIData" + time + ".txt";
        String file_path = Environment.getExternalStorageDirectory().getPath() + "/Empa/" + fileName;
        try (InputStream in = new FileInputStream(fileName)) {
            FileMetadata metadata = client.files().uploadBuilder(file_path)
                    .uploadAndFinish(in);
        }

*/
    }
}