package io.s4.ft;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

public class DefaultFileSystemStateStorage implements StateStorage, KeyStorage {

    private static Logger LOG = Logger.getLogger(DefaultFileSystemStateStorage.class);
    private String storageRootPath;

    @Override
    public void saveState(String key, byte[] state, StorageCallback callback) {
        // TODO asynchronous

        File f = key2File(key);
        if (!f.exists()) {
            if (!f.getParentFile().exists()) {
                // parent file has prototype id
                if (!f.getParentFile().mkdir()) {
                    callback.storageOperationResult(SafeKeeper.StorageResultCode.FAILURE,
                            "Cannot create directory for storing PE for prototype: "
                                    + f.getParentFile().getAbsolutePath());
                    return;
                }
            }
            // TODO handle IO exception
            try {
                f.createNewFile();
            } catch (IOException e) {
                callback.storageOperationResult(SafeKeeper.StorageResultCode.FAILURE, e.getMessage());
            }
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            fos.write(state);
        } catch (FileNotFoundException e) {
            callback.storageOperationResult(SafeKeeper.StorageResultCode.FAILURE, e.getMessage());
        } catch (IOException e) {
            callback.storageOperationResult(SafeKeeper.StorageResultCode.FAILURE, e.getMessage());
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                LOG.error(e);
            }
        }

    }

    @Override
    public byte[] fetchState(String key) {
        File file = key2File(key);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Fetching " + file.getAbsolutePath() + "for : " + key);
        }
        if (file != null && file.exists()) {

            // TODO use commons-io or guava
            FileInputStream in = null;
            try {
                in = new FileInputStream(file);

                long length = file.length();

                /*
                 * Arrays can only be created using int types, so ensure that the file size is not too big before we
                 * downcast to create the array.
                 */
                if (length > Integer.MAX_VALUE) {
                    throw new IOException("Error file is too large: " + file.getName() + " " + length + " bytes");
                }

                byte[] buffer = new byte[(int) length];
                int offSet = 0;
                int numRead = 0;

                while (offSet < buffer.length && (numRead = in.read(buffer, offSet, buffer.length - offSet)) >= 0) {
                    offSet += numRead;
                }

                if (offSet < buffer.length) {
                    throw new IOException("Error, could not read entire file: " + file.getName() + " " + offSet + "/"
                            + buffer.length + " bytes read");
                }

                in.close();
                return buffer;
            } catch (FileNotFoundException e1) {
                LOG.error(e1);
            } catch (IOException e2) {
                LOG.error(e2);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e) {
                        LOG.warn(e);
                    }
                }
            }
        }
        return null;

    }

    @Override
    public Set<String> fetchStoredKeys() {
        Set<String> keys = new HashSet<String>();
        File rootDir = new File(storageRootPath);
        File[] dirs = rootDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        for (File dir : dirs) {
            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return (file.isFile());
                }
            });
            for (File file : files) {
                keys.add(file2key(file));
            }
        }
        return keys;
    }

    @Override
    public void addKey(String key) {
        // do nothing, keys are already stored in the filesystem when saveState() is called
    }

    // the key is the file name itself
    private File key2File(String key) {
        return new File(storageRootPath + File.separator + key);
    }

    // the key is the file name itself
    private String file2key(File file) {
        return file.getName();
    }

    public String getStorageRootPath() {
        return storageRootPath;
    }

    public void setStorageRootPath(String storageRootPath) {
        this.storageRootPath = storageRootPath;
        File rootPathFile = new File(storageRootPath);
        if (!rootPathFile.exists()) {
            if (!rootPathFile.mkdirs()) {
                LOG.error("could not create root storage directory : " + storageRootPath);
            }

        }
    }

    public void checkStorageDir() {
        if (storageRootPath == null) {

            File defaultStorageDir = new File(System.getProperty("user.dir") + File.separator + "tmp" + File.separator
                    + "storage");
            storageRootPath = defaultStorageDir.getAbsolutePath();
            if (LOG.isInfoEnabled()) {
                LOG.info("Unspecified storage dir; using default dir: " + defaultStorageDir.getAbsolutePath());
            }
            if (!defaultStorageDir.exists()) {
                if (!(defaultStorageDir.mkdirs())) {
                    LOG.error("Storage directory not specified, and cannot create default storage directory : "
                            + defaultStorageDir.getAbsolutePath());
                    // TODO exit?
                    System.exit(-1);
                }
            }
        }
    }
}
