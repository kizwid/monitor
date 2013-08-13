package kizwid.shared.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

/**
 * Scans a directory and notifies observers when it finds files/directories.
 */
public class FileWatcher implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(FileWatcher.class);
	private static final long MAX_TIME_SINCE_LAST_MODIFIED = 1000 * 60 * 60 * 24;	// 1 day

	private File dir;
	private boolean watchDirs;
	private String nameRegexp;

	private long pollingIntervalMillis = 1000;
	private volatile boolean stopRequested = false;
	private Map<File, Long> prevModifiedTimeByFile = new HashMap<File, Long>();

	private List<FileWatcherListener> listeners =
		Collections.synchronizedList(new ArrayList<FileWatcherListener>());

	/**
	 * Constructor to watch for new files.
	 * @param dir directory in which to watch for (sub)directory creations.
	 * @param watchDirs if true watch (sub)directories, if false watch files.
	 * @param nameRegexp regexp for matching watched files.
	 * @param pollingIntervalMillis number of milliseconds between polls.
	 * @throws NullPointerException if directory or filename regexp is null.
	 */
	public FileWatcher(File dir, boolean watchDirs, String nameRegexp, long pollingIntervalMillis) {
		if (dir == null) {
			throw new NullPointerException("dir");
		}

		if (nameRegexp == null) {
			throw new NullPointerException("fileNameRegexp");
		}

		this.dir = dir;
		this.watchDirs = watchDirs;
		this.nameRegexp = nameRegexp;
		this.pollingIntervalMillis = pollingIntervalMillis;
	}

	public synchronized void stop() {
		stopRequested = true;
	}

	private boolean stopHasBeenRequested() {
		// don't really need to synchronize here
		// - worst case scenario is that we miss a stop request and pick it up on the next poll
		return stopRequested;
	}

	public void addListener(FileWatcherListener listener) {
		// listeners is a synchronized list - no need to synchronize here
		listeners.add(listener);
	}

	public void removeListener(FileWatcherListener listener) {
		// listeners is a synchronized list - no need to synchronize here
		listeners.remove(listener);
	}

	public void run() {

        // now that thread is running, check that directory exists
        // doing this test here allows it to be created subsequent to the file watcher construction
        if (! dir.exists()) {
            throw new RuntimeException("FileWatcher directory path does not exist[" + dir + "]");
        }

        for (FileWatcherListener listener : listeners) {
            listener.startedWatching(dir);
        }

        // record time we started polling
        long startPollTime = System.currentTimeMillis();

        for (;;) {
			logger.debug("starting directory poll {}", dir);

			// has an external thread requested that this one be stopped?
			if (stopHasBeenRequested()) {
				logger.info("stopping file watcher thread");
				break;
			}

			// scan directory for files or directories we are interested in
			File[] files = dir.listFiles(new FileFilter() {
				public boolean accept(File file) {
					boolean isDir = file.isDirectory();
					if (watchDirs != isDir)
						return false;

					return file.getName().matches(nameRegexp);
				}
			});

			// find directory entries that have been changed since previous poll
			// note: given that even the retrieval of the last modified time can consume time
			// (let alone any actions in the notifications), it is not safe to just compare the
			// current last modified time of each file against a single time for the end of the
			// previous poll - instead we maintain a record of the previous modification time
			// for any file which has been modified during the lifetime of this poller
			// then compare the current last modified time against that
			for (File file: files) {
				logger.debug("found " + file.getName());

				// find previous known last modified time for file
				Long prevModifiedTime = prevModifiedTimeByFile.get(file);
				if (prevModifiedTime == null) {
					// we do not know about this file - this means that it had not previously
					// been modified since we started polling so use start of polling as its
					// previous modification time
                    prevModifiedTimeByFile.put(file, prevModifiedTime = startPollTime);
					logger.debug("no previous modification time for file {}", file);
				} else if ((System.currentTimeMillis() - prevModifiedTime) > MAX_TIME_SINCE_LAST_MODIFIED) {
					// the last time this file was modified since we started up this poller
					// was over (default) a day ago - we can't possibly get confused over a
					// modification time that long ago so get rid of file from cache
					prevModifiedTimeByFile.remove(file);
					logger.debug("cache entry expired, ignoring file {}", file);
					continue;
				}
				logger.debug("prevModifiedTime={} for file {}", prevModifiedTime, file);

				long lastModifiedTime = file.lastModified();
				logger.debug("lastModifiedTime={} for file {}", lastModifiedTime, file);
				if (lastModifiedTime > prevModifiedTime) {
					notifyFileChange(file);
					prevModifiedTimeByFile.put(file, lastModifiedTime);
				}
			}

			try {
				Thread.sleep(pollingIntervalMillis);
			} catch (InterruptedException exp) {
			}
		}
	}

	private void notifyFileChange(File file) {
		logger.info("Detected change to " + file);
		notifyListeners(file);
		logger.debug("end listener notification for " + file);
	}

	private void notifyListeners(File file) {
		// listeners is a synchronized list - no need to sync here
		for (FileWatcherListener listener: listeners) {
			try {
				listener.fileChanged(file);
			} catch (Exception e) {
				logger.info("Discarding exception " + e.getLocalizedMessage() + " thrown by FileWatcherListener");
			}
		}
	}


    /*
    *
    private final File fileOutbox, fileProcessedDir, fileInTransitDir;
    private int fileSizeChangeCheckIntervalMilliSeconds = 2000; //2 seconds
    private int outboxScanIntervalMilliSeconds = 10000; //10 seconds

    public FilePublisher(JmsBean jmsBean) throws Exception {

        this.jmsBean = jmsBean;

        fileOutbox = new File((String) jmsBean.getMapConfig().get("OutboxPath"));
        if (!fileOutbox.exists()) {
            throw new Exception("FilePublisher.OutboxPath[" + fileOutbox + "] does not exit");
        }
        fileProcessedDir = new File((String) jmsBean.getMapConfig().get("ProcessedPath"));
        fileInTransitDir = new File(fileProcessedDir, "in_transit");

        //default is false (unless "true" is specified)
        isKeepAlive = "true".equals(jmsBean.getMapConfig().get("KeepAlive"));
        try {
            outboxScanIntervalMilliSeconds = Integer.parseInt((String) jmsBean.getMapConfig().get("OutboxScanIntervalMilliSeconds"));
        } catch (NumberFormatException nfe) {
            logger.info("OutboxScanIntervalSeconds not specified. using default value[" + outboxScanIntervalMilliSeconds + "]");
        }
        try {
            fileSizeChangeCheckIntervalMilliSeconds = Integer.parseInt((String) jmsBean.getMapConfig()
                    .get("FileSizeChangeCheckIntervalMilliSeconds"));
        } catch (NumberFormatException nfe) {
            logger.info("FileSizeChangeCheckIntervalMilliSeconds not specified. using default value["
                    + fileSizeChangeCheckIntervalMilliSeconds + "]");
        }

        //on startup move all in_transit files back to Outbox
        File[] afileInTransit = fileInTransitDir.listFiles();
        if (afileInTransit == null) {
            afileInTransit = new File[0]; //avoid null pointer
        }

        for (File element : afileInTransit) {
            File fileInTransit = element;
            File fileInOutbox = new File(fileOutbox, fileInTransit.getName());
            if (fileInOutbox.exists()) {
                fileInOutbox.delete();
            }
            System.gc();
            while (!fileInTransit.renameTo(fileInOutbox)) {
                logger.error("Error in_transit file [" + fileInTransit.getName() + "] move to Outbox - retry");
            }
            logger.info("in_transit file [" + fileInTransit.getName() + "] moved to Outbox");
        }
    }

    public void run() {

        logger.info("started watching folder[" + fileOutbox + "]");
        do {
            try {
                logger.debug("checking for new files");
                sendOutgoingFiles(jmsBean, resolveJobId());
            } catch (Exception e) {
                logger.error("BOOM", e);
                break; //let the thread die
            }

            try {
                Thread.sleep(outboxScanIntervalMilliSeconds);
            } catch (Exception exp) {
            }

        } while (isKeepAlive);

        logger.info("FilePublisher stopped");
        System.exit(1);

    }

    public void start() throws Exception {

        jmsBean.init();
        Thread threadKeepAlive = new Thread(this);
        threadKeepAlive.setName(getJmsBeanName() + " keepAlive");
        threadKeepAlive.start();

    }

    private String resolveJobId() {
        Date businessDate = new Date();
        return DateFormatUtil.yyyymmdd(businessDate) + "-"
                + DateFormatUtil.dateFormatted(businessDate, "HHmmss") + "-"
                + getJmsBeanName();
    }

    private void sendOutgoingFiles(JmsBean jmsBean, String jobId) throws Exception {

        //create list of all files in outgoing folder
        File[] afile = fileOutbox.listFiles(new FileFilter()
                {
                    public boolean accept(File p_file)
                {
                    if (p_file.isDirectory()) {
                        return false;
                    }
                else {
                    //return !p_file.getName().endsWith( ".tmp");
                    return true;
                }
            }
                });

        //exit here if no files to publish
        if (afile == null || afile.length == 0) {
            logger.debug("no files to publish");
            return; //nothing to do
        }

        //convert array to List
        List<File> listFile = new LinkedList<File>();
        listFile.addAll(Arrays.asList(afile));

        //wait here for a bit to see if the file is still being written to
        TreeMap<File, Long> mapFile2Size = new TreeMap<File, Long>();
        for (int sizeTestLoop = 0; sizeTestLoop < 3 && listFile.size() > 0; sizeTestLoop++) {
            for (Iterator itor = listFile.iterator(); itor.hasNext();) {
                File file = (File) itor.next();
                Long previousSize = mapFile2Size.get(file);
                long size = file.length();
                if (previousSize == null) {
                    previousSize = new Long(size); //first loop nothing to compare with
                }

                if (size != previousSize) {
                    itor.remove(); //file size changed so don't send this one
                    logger.info("change of file size detected [" + file + "] - will check this one later");
                }

                mapFile2Size.put(file, new Long(size));
            }

            Thread.sleep(fileSizeChangeCheckIntervalMilliSeconds);
        }

        //exit here if no files to publish
        if (listFile.size() == 0) {
            logger.debug("no files to publish - waiting for stable file size");
            return; //nothing to do
        }

        //make sure we have a staging area to move files that are in transit
        if (!fileInTransitDir.exists()) {
            if (!fileInTransitDir.mkdirs()) {
                isKeepAlive = false;
                throw new Exception("failed to create transit staging folder[" + fileInTransitDir + "]");
            }
        }

        //prepare new list of files
        //which we think we are able to send
        List<File> listFileToSend = new LinkedList<File>();

        //check that we have exclusive access to files by moving them to a staging area
        for (File file : listFile) {
            File fileMoved = new File(fileInTransitDir, file.getName());
            if (fileMoved.exists()) {
                logger.debug("deleting existing file [{}]", fileMoved);
                boolean deleted = fileMoved.delete();
                if (!deleted) {
                    isKeepAlive = false;
                    throw new Exception("failed to delete existing file[" + fileMoved + "] from staging area");
                }
            }

            boolean moved = file.renameTo(fileMoved);
            if (moved) {
                listFileToSend.add(fileMoved);
            } else {
                logger.info("unable to secure exclusive access to file [" + file + "] - will try again later.");
            }
        }

        //drop out here if we can't get exclusive access to any of our files
        if (listFileToSend.size() == 0) {
            logger.debug("no files to publish - waiting for exclusive access");
            return; //nothing to do
        }

        Message msg = jmsBean.createMessage(jobId, listFileToSend);
        jmsBean.publish(msg);
        System.gc(); //free file held by argon message sender

        //we keep sent messages in folders per jobId
        File fileProcessedJobDir = new File(fileProcessedDir, jobId);

        //make sure we have somewhere to move processed messages to
        if (!fileProcessedJobDir.exists()) {
            if (!fileProcessedJobDir.mkdirs()) {
                isKeepAlive = false;
                throw new Exception("failed to create processed folder[" + fileProcessedJobDir + "]");
            }
        }

        for (File file : listFileToSend) {
            File fileMoved = new File(fileProcessedJobDir, file.getName());
            if (fileMoved.exists()) {
                logger.debug("deleting existing file [{}]", fileMoved);
                boolean deleted = fileMoved.delete();
                if (!deleted) {
                    isKeepAlive = false;
                    throw new Exception("failed to delete existing file[" + fileMoved + "]");
                }
            }

            int nRetry = 0;
            while (!file.renameTo(fileMoved)) {
                logger.warn("error moving " + file + " - retry " + nRetry);
                if (!file.exists()) {
                    break;
                }
                nRetry++;
                if (nRetry > 3) {
                    isKeepAlive = false;
                    throw new Exception("failed to move file[" + file + "] to processed folder[" + fileProcessedJobDir + "]");
                }
                Thread.sleep(2000);
            }

            logger.info("published file [" + fileMoved + "]");

        }

        logger.info("published message[" + jobId + "] with " + listFileToSend.size() + " files");

    }

    protected String getJmsBeanName() {
        return jmsBean.getName();
    }

    *
    *
    * */




}

