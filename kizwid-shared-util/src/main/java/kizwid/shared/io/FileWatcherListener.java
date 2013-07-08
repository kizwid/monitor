package kizwid.shared.io;

import java.io.File;

/**
 * Listener for notifications from {@link FileWatcher}.
 */
public interface FileWatcherListener {
    void startedWatching(File directory);
	void fileChanged(File file);
}
