package kizwid.shared.io;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: kevsanders
 * Date: 07/07/2013
 * Time: 23:37
 * To change this template use File | Settings | File Templates.
 */
public class FileWatcherTest {
    private static final String TEST_DIR_NAME = "fileWatcherTest";
    private static final String NAME_REGEXP = "xyz.*";
    private static final long POLLING_INTERVAL_MILLIS = 10L;
    private static final long TIMEOUT_MILLIS = 50L;

    private static Logger logger = LoggerFactory.getLogger(FileWatcherTest.class);

    private File testDirectory;
    private FileWatcher watcher;
    private Listener listener;

    private class Listener implements FileWatcherListener {
        private File foundFile;
        private final CountDownLatch latch;

        public Listener(CountDownLatch latch) {
            this.latch = latch;
        }

        public synchronized void reset() {
            foundFile = null;
        }

        public synchronized File getFoundFile() {
            return foundFile;
        }

        @Override
        public synchronized void fileChanged(File file) {
            foundFile = file;
        }

        @Override
        public void startedWatching(File directory) {
            latch.countDown();//notify test that watcher thread has started
        }
    }

    @Before
    public void prepare() throws IOException {
        // locate package compiled classes directory - we'll create our test files here
        Resource packageDirResource = new ClassPathResource("", getClass());
        File packageDir = packageDirResource.getFile();

        // delete and recreate test directory
        testDirectory = new File(packageDir, TEST_DIR_NAME);
        FileUtils.deleteDirectory(testDirectory);
        testDirectory.mkdirs();
    }

    @After
    public void tidyUp() throws IOException {
        // if we have watcher tell it to stop just in case it has been left running
        if (watcher != null) {
            watcher.stop();
        }

        // clean up directory (even though 'prepare' also does this)
        FileUtils.deleteDirectory(testDirectory);
    }

    @Test
    public void canOnlyDetectFilesCreatedAfterStartOfWatch() throws IOException, InterruptedException {
        canOnlyDetectCreationAfterStartOfWatch(false);
    }

    @Test
    public void canOnlyDetectDirectoriesCreatedAfterStartOfWatch() throws IOException, InterruptedException {
        canOnlyDetectCreationAfterStartOfWatch(true);
    }

    private void canOnlyDetectCreationAfterStartOfWatch(boolean isDir) throws IOException, InterruptedException {

        // create file prior to starting up watcher
        File file1 = new File(testDirectory, "xyz1");
        create(file1, isDir);
        file1.setLastModified(System.currentTimeMillis()-1000);

        // set watcher thread running
        createWatcher(isDir);

        // should not detect file1 within timeout period
        assertFalse(tryToDetect(file1));

        // create file2 and detect it
        File file2 = new File(testDirectory, "xyz2");
        listener.reset();
        create(file2, isDir);
        assertTrue(tryToDetect(file2));
    }

    @Test
    public void canDetectModificationToFileOnlyOnce() throws IOException, InterruptedException {
        canDetectModificationOnlyOnce(false);
    }

    @Test
    public void canDetectModificationToDirectoryOnlyOnce() throws IOException, InterruptedException {
        canDetectModificationOnlyOnce(true);
    }

    private void canDetectModificationOnlyOnce(boolean isDir) throws IOException, InterruptedException {
        // set watcher thread running
        createWatcher(isDir);

        // create file and detect it
        File file1 = new File(testDirectory, "xyz1");
        listener.reset();
        logger.info("creating file {} at {}", file1, System.currentTimeMillis());
        create(file1, isDir);
        logger.info("created file {}", file1);
        assertTrue(tryToDetect(file1));

        // cannot detect file again
        listener.reset();
        assertFalse(tryToDetect(file1));
    }

    @Test
    public void canDetectMultipleModificationsToFile() throws IOException, InterruptedException {
        canDetectMultipleModifications(false);
    }

    @Test
    public void canDetectMultipleModificationsToDir() throws IOException, InterruptedException {
        canDetectMultipleModifications(true);
    }

    private void canDetectMultipleModifications(boolean isDir) throws IOException, InterruptedException {
        // set watcher thread running
        createWatcher(isDir);

        // create file and detect it
        File file1 = new File(testDirectory, "xyz1");
        listener.reset();
        create(file1, isDir);
        assertTrue(tryToDetect(file1));

        // modify file and detect change
        listener.reset();
        modify(file1, isDir);
        assertTrue(tryToDetect(file1));
    }

    @Test
    public void ignoresModificationsToNonMatchingFiles() throws IOException, InterruptedException {
        ignoresModificationsToNonMatching(false);
    }

    @Test
    public void ignoresModificationsToNonMatchingDirs() throws IOException, InterruptedException {
        ignoresModificationsToNonMatching(true);
    }

    private void ignoresModificationsToNonMatching(boolean isDir) throws IOException, InterruptedException {
        // set watcher thread running
        createWatcher(isDir);

        // create non-matching file and validate that it is not detected
        File file1 = new File(testDirectory, "not-xyz1");
        listener.reset();
        create(file1, isDir);
        assertFalse(tryToDetect(file1));

        // modify file and validate that change is still not detected
        listener.reset();
        modify(file1, isDir);
        assertFalse(tryToDetect(file1));
    }

    private void createWatcher(boolean isDir) throws InterruptedException {
        watcher = new FileWatcher(testDirectory, isDir, NAME_REGEXP, POLLING_INTERVAL_MILLIS);

        CountDownLatch latch = new CountDownLatch(1);
        listener = new Listener(latch);
        watcher.addListener(listener);

        new Thread(watcher).start();
        latch.await();

    }

    private boolean tryToDetect(File expectedFile) {
        // check for detection of file every poller interval
        logger.info("trying to detect " + expectedFile);
        long elapsedTime = 0;
        for (; ; ) {
            waitForOnePoll();

            File foundFile = listener.getFoundFile();
            if (foundFile != null) {
                logger.info("found file " + foundFile);
                return expectedFile.equals(foundFile);
            }

            elapsedTime += POLLING_INTERVAL_MILLIS;
            if (elapsedTime > TIMEOUT_MILLIS) {
                logger.info("hit timeout - file not found");
                return false;
            }
        }
    }

    private void waitForOnePoll() {
        try {
            Thread.sleep(POLLING_INTERVAL_MILLIS);
        } catch (InterruptedException e) {
            logger.info("interrupted!");
        }
    }

    private void create(File file, boolean isDir) throws IOException {
        if (isDir) {
            file.mkdirs();
        } else {
            file.createNewFile();
        }
        modify(file, isDir);
    }

    private void modify(File file, boolean isDir) throws IOException {
        file.setLastModified(file.lastModified()+4000);
    }

}
