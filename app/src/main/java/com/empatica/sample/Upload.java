package com.empatica.sample;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

public class Upload {
    private static final String ACCESS_TOKEN = "lY_d3DAmzgAAAAAAAAAAdn7IAKZUPyYJXhaYmllRlCFZwhYgt_m6fNafXq8DcgWK";

    public static void upload(String path, String fileName) throws DbxException, IOException {

        /**
         * Create Dropbox client
         */
        DbxRequestConfig config = new DbxRequestConfig("dropbox/E4Link");
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);


        /**
         * Get current account info
         */
        FullAccount account = client.users().getCurrentAccount();
        System.out.println(account.getName().getDisplayName());

        /**
         * Get files and folder metadata from Dropbox root directory
        */
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

        /**
         * Upload IBI data file to Dropbox
         */

        File file = new File(path);
        try (InputStream in = new FileInputStream(file)) {
            FileMetadata metadata = client.files().uploadBuilder("/IBI/"+fileName)
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(in);
        }
        catch (DbxException ex) {
            System.out.println(ex.getMessage());
        }

    }
}